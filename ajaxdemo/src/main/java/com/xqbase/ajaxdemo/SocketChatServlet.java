package com.xqbase.ajaxdemo;

import java.io.IOException;

import javax.websocket.OnMessage;
import javax.websocket.Session;
import javax.websocket.server.ServerEndpoint;

import com.xqbase.util.Log;

@ServerEndpoint("/socketchat/")
public class SocketChatServlet {
	@OnMessage
	public static void onMessage(Session session, String message) {
		for (Session session_ : session.getOpenSessions()) {
			if (session_.isOpen()) {
				try {
					session_.getBasicRemote().sendText(message);
				} catch (IOException e) {
					Log.w(e.getMessage());
				}
			}
		}
	}
}