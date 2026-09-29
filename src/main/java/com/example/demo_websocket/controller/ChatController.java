package com.example.demo_websocket.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.stereotype.Controller;

import com.example.demo_websocket.model.ChatMessage;

@Controller
public class ChatController {

	private static final Logger log = LoggerFactory.getLogger(ChatController.class);

	/**
	 * Reçoit les trames SEND adressées à /app/chat (préfixe "/app" + "/chat"),
	 * puis rediffuse le message à tous les abonnés de /topic/messages.
	 */
	@MessageMapping("/chat")
	@SendTo("/topic/messages")
	public ChatMessage handleChatMessage(@Payload ChatMessage message, SimpMessageHeaderAccessor headers) {
		log.info("[STOMP {}] Message reçu sur /app/chat -> sender='{}', content='{}'",
				headers.getSessionId(), message.getSender(), message.getContent());
		return message;
	}

}
