package chat;

import java.util.ArrayList;

public class ChatSessions {
	private final String type = "SESSION_LIST";

	private ArrayList<ChatSession> sessions;

	public ChatSessions() {
		sessions = new ArrayList<ChatSession>();
	}

	public void add(ChatSession session) {
		sessions.add(session);
	}
}
