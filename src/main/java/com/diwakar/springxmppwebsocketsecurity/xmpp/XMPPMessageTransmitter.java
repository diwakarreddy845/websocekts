package com.diwakar.springxmppwebsocketsecurity.xmpp;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jivesoftware.smack.packet.Message;
import org.springframework.stereotype.Component;

import com.diwakar.springxmppwebsocketsecurity.model.WebsocketMessage;
import com.diwakar.springxmppwebsocketsecurity.websocket.utils.WebSocketTextMessageHelper;

import static com.diwakar.springxmppwebsocketsecurity.model.MessageType.NEW_MESSAGE;

import javax.websocket.Session;

@Slf4j
@Component
@RequiredArgsConstructor
public class XMPPMessageTransmitter {

    private final WebSocketTextMessageHelper webSocketTextMessageHelper;

    public void sendResponse(Message message, Session session) {
        log.info("New message from '{}' to '{}': {}", message.getFrom(), message.getTo(), message.getBody());
        String messageFrom = message.getFrom().getLocalpartOrNull().toString();
        String to = message.getTo().getLocalpartOrNull().toString();
        String content = message.getBody();
        webSocketTextMessageHelper.send(
                session,
                WebsocketMessage.builder()
                        .from(messageFrom)
                        .to(to)
                        .content(content)
                        .messageType(NEW_MESSAGE).build()
        );
    }
}
