package chat;

public class ChatMessage {
	private final String type = "MESSAGE";

	public String sessionName;
	public String message;

	ChatMessage(String sessionName, String message) {
		this.sessionName = sessionName;
		this.message = message;
	}

}
