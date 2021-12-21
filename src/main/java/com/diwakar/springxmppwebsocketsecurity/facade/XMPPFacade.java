package com.diwakar.springxmppwebsocketsecurity.facade;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jivesoftware.smack.packet.Presence;
import org.jivesoftware.smack.roster.RosterEntry;
import org.jivesoftware.smack.tcp.XMPPTCPConnection;
import org.springframework.boot.configurationprocessor.json.JSONArray;
import org.springframework.stereotype.Component;

import com.diwakar.springxmppwebsocketsecurity.exception.XMPPGenericException;
import com.diwakar.springxmppwebsocketsecurity.model.Account;
import com.diwakar.springxmppwebsocketsecurity.model.WebsocketMessage;
import com.diwakar.springxmppwebsocketsecurity.service.AccountService;
import com.diwakar.springxmppwebsocketsecurity.utils.BCryptUtils;
import com.diwakar.springxmppwebsocketsecurity.websocket.utils.WebSocketTextMessageHelper;
import com.diwakar.springxmppwebsocketsecurity.xmpp.XMPPClient;

import javax.websocket.Session;

import static com.diwakar.springxmppwebsocketsecurity.model.MessageType.ERROR;
import static com.diwakar.springxmppwebsocketsecurity.model.MessageType.FORBIDDEN;
import static com.diwakar.springxmppwebsocketsecurity.model.MessageType.GET_CONTACTS;
import static com.diwakar.springxmppwebsocketsecurity.model.MessageType.JOIN_SUCCESS;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

@Slf4j
@Component
@RequiredArgsConstructor
public class XMPPFacade {

	private static final Map<Session, XMPPTCPConnection> CONNECTIONS = new HashMap<>();

	private final AccountService accountService;
	private final WebSocketTextMessageHelper webSocketTextMessageHelper;
	private final XMPPClient xmppClient;

	public void startSession(Session session, String username, String password) {
		
		Optional<Account> account = accountService.getAccount(username);

		if (account.isPresent() && !BCryptUtils.isMatch(password, account.get().getPassword())) {
			log.warn("Invalid password for user {}.", username);
			webSocketTextMessageHelper.send(session, WebsocketMessage.builder().messageType(FORBIDDEN).build());
			return;
		}

		Optional<XMPPTCPConnection> connection = xmppClient.connect(username, password);

		if (connection.isEmpty()) {
			webSocketTextMessageHelper.send(session, WebsocketMessage.builder().messageType(ERROR).build());
			return;
		}

		try {
			if (account.isEmpty()) {
				xmppClient.createAccount(connection.get(), username, password);
			}
			xmppClient.login(connection.get());
		} catch (XMPPGenericException e) {
			handleXMPPGenericException(session, connection.get(), e);
			return;
		}

		CONNECTIONS.put(session, connection.get());
		log.info("Session was stored.");

		xmppClient.addIncomingMessageListener(connection.get(), session);

		webSocketTextMessageHelper.send(session, WebsocketMessage.builder().to(username).messageType(JOIN_SUCCESS).build());
	}

	public void sendMessage(WebsocketMessage message, Session session) {
		XMPPTCPConnection connection = CONNECTIONS.get(session);

		if (connection == null) {
			return;
		}

		switch (message.getMessageType()) {
		case NEW_MESSAGE: {
			try {
				xmppClient.sendMessage(connection, message.getContent(), message.getTo());
			} catch (XMPPGenericException e) {
				handleXMPPGenericException(session, connection, e);
			}
		}
			break;
		case ADD_CONTACT: {
			try {
				xmppClient.addContact(connection, message.getTo());
			} catch (XMPPGenericException e) {
				handleXMPPGenericException(session, connection, e);
			}
		}
			break;
		case GET_CONTACTS: {
			Set<RosterEntry> contacts = Set.of();
			try {
				contacts = xmppClient.getContacts(connection);
			} catch (XMPPGenericException e) {
				handleXMPPGenericException(session, connection, e);
			}

			JSONArray jsonArray = new JSONArray();
			for (RosterEntry entry : contacts) {
				jsonArray.put(entry.getName());
			}
			WebsocketMessage responseMessage = WebsocketMessage.builder().content(jsonArray.toString()).messageType(GET_CONTACTS).build();
			log.info("Returning list of contacts {} for user {}.", jsonArray, connection.getUser());
			webSocketTextMessageHelper.send(session, responseMessage);
		}
			break;
		default:
			break;

		}
	}

	public void disconnect(Session session) {
		XMPPTCPConnection connection = CONNECTIONS.get(session);

		if (connection == null) {
			return;
		}

		try {
			xmppClient.sendStanza(connection, Presence.Type.unavailable);
		} catch (XMPPGenericException e) {
			log.error("XMPP error.", e);
			webSocketTextMessageHelper.send(session, WebsocketMessage.builder().messageType(ERROR).build());
		}

		xmppClient.disconnect(connection);
		CONNECTIONS.remove(session);
	}

	private void handleXMPPGenericException(Session session, XMPPTCPConnection connection, Exception e) {
		log.error("XMPP error. Disconnecting and removing session...", e);
		xmppClient.disconnect(connection);
		webSocketTextMessageHelper.send(session, WebsocketMessage.builder().messageType(ERROR).build());
		CONNECTIONS.remove(session);
	}
}
