/*
 *  Licensed to the Apache Software Foundation (ASF) under one or more
 *  contributor license agreements.  See the NOTICE file distributed with
 *  this work for additional information regarding copyright ownership.
 *  The ASF licenses this file to You under the Apache License, Version 2.0
 *  (the "License"); you may not use this file except in compliance with
 *  the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package chat;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.concurrent.atomic.AtomicInteger;

import javax.websocket.OnClose;
import javax.websocket.OnError;
import javax.websocket.OnMessage;
import javax.websocket.OnOpen;
import javax.websocket.Session;
import javax.websocket.server.ServerEndpoint;

import org.apache.juli.logging.Log;
import org.apache.juli.logging.LogFactory;

import com.google.gson.Gson;

import util.HTMLFilter;

@ServerEndpoint(value = "/websocket/chat")
public class WebSocketConnection {

	private static final Log log = LogFactory.getLog(WebSocketConnection.class);

	private static final String GUEST_PREFIX = "Client";
	private static final AtomicInteger connectionIds = new AtomicInteger(0);
	private static final Set<WebSocketConnection> connections = new CopyOnWriteArraySet<>();
	private static final Gson gson = new Gson();

	private final String nickname;
	private Session session;

	public WebSocketConnection() {
		nickname = GUEST_PREFIX + connectionIds.getAndIncrement();
	}

	@OnOpen
	public void start(Session session) {
		this.session = session;
		connections.add(this);
		String message = String.format("* %s %s", nickname, "has joined.");
		broadcast(nickname, message);
		broadcastActiveSessions();
	}

	@OnClose
	public void end() {
		connections.remove(this);
		String message = String.format("* %s %s", nickname, "has disconnected.");
		broadcast(nickname, message);
		broadcastActiveSessions();
	}

	@OnMessage
	public void incoming(String message) {
		// Never trust the client
		// String filteredMessage = String.format("%s: %s", nickname, HTMLFilter.filter(message.toString()));
		String filteredMessage = HTMLFilter.filter(message.toString());
		broadcast(nickname, filteredMessage);
	}

	@OnError
	public void onError(Throwable t) throws Throwable {
		log.error("Chat Error: " + t.toString(), t);
	}

	// Broadcast to all clients a client's name and his message
	private static void broadcast(String clientName, String msg) {
		for (WebSocketConnection client : connections) {
			try {
				synchronized (client) {
					ChatMessage message = new ChatMessage(clientName, msg);
					client.session.getBasicRemote().sendText(gson.toJson(message));
				}
			} catch (IOException e) {
				log.debug("Chat Error: Failed to send message to client", e);
				connections.remove(client);
				try {
					client.session.close();
				} catch (IOException e1) {
					// Ignore
				}
				String message = String.format("* %s %s", client.nickname, "has been disconnected.");
				broadcast(client.nickname, message);
			}
		}
	}

	// Broadcast to all clients the list of active client names
	private static void broadcastActiveSessions() {
		// marshal list of active sessions
		ArrayList<ActiveSession> activeSessions = new ArrayList<ActiveSession>();
		for (WebSocketConnection client : connections) {
			activeSessions.add(new ActiveSession(client.nickname));
		}

		// broadcast list of active sessions to all clients
		for (WebSocketConnection client : connections) {
			try {
				synchronized (client) {
					System.out.println("Sending activeSessions to client ");
					log.info("Sending activeSessions to client ");
					client.session.getBasicRemote().sendText(gson.toJson(activeSessions));
				}
			} catch (IOException e) {
				System.out.println("Chat Error: IO exception in activeSessions");
				log.error("Chat Error: IO exception in activeSessions", e);
			}
		}
	}
}
