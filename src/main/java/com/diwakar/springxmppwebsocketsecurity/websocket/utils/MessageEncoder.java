package com.diwakar.springxmppwebsocketsecurity.websocket.utils;

import com.diwakar.springxmppwebsocketsecurity.model.WebsocketMessage;
import com.google.gson.Gson;

import javax.websocket.Encoder;
import javax.websocket.EndpointConfig;

public class MessageEncoder implements Encoder.Text<WebsocketMessage> {
    @Override
    public String encode(WebsocketMessage message) {
        Gson gson = new Gson();
        return gson.toJson(message);
    }

    @Override
    public void init(EndpointConfig config) {

    }

    @Override
    public void destroy() {

    }
}
