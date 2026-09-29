package com.example.demo_websocket.model;

/**
 * Message échangé entre le client (Postman) et le serveur.
 * <p>
 * Corps JSON attendu : {@code {"sender":"Alice","content":"Bonjour"}}
 */
public class ChatMessage {

	private String sender;   // qui envoie le message
	private String content;  // texte du message

	public ChatMessage() {
		// constructeur vide requis par Jackson pour convertir le JSON en objet
	}

	public ChatMessage(String sender, String content) {
		this.sender = sender;
		this.content = content;
	}

	public String getSender() {
		return sender;
	}

	public void setSender(String sender) {
		this.sender = sender;
	}

	public String getContent() {
		return content;
	}

	public void setContent(String content) {
		this.content = content;
	}

	@Override
	public String toString() {
		return "ChatMessage{sender='" + sender + "', content='" + content + "'}";
	}

}
