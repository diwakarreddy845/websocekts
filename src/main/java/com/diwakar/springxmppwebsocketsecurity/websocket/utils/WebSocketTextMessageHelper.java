package com.diwakar.springxmppwebsocketsecurity.websocket.utils;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import com.diwakar.springxmppwebsocketsecurity.model.WebsocketMessage;

import javax.websocket.EncodeException;
import javax.websocket.Session;
import java.io.IOException;

@Slf4j
@Component
public class WebSocketTextMessageHelper {

    public void send(Session session, WebsocketMessage websocketMessage) {
        try {
            session.getBasicRemote().sendObject(websocketMessage);
        } catch (IOException | EncodeException e) {
            log.error("WebSocket error, message {} was not sent.", websocketMessage.toString(), e);
        }
    }
}
