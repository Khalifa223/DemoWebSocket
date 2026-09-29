package com.example.demo_websocket.listener;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.messaging.SessionConnectEvent;
import org.springframework.web.socket.messaging.SessionConnectedEvent;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;
import org.springframework.web.socket.messaging.SessionSubscribeEvent;

/**
 * Écoute les événements STOMP publiés par Spring (connexion, abonnement,
 * déconnexion) pour les afficher dans la console.
 */
@Component
public class WebSocketEventListener {

	private static final Logger log = LoggerFactory.getLogger(WebSocketEventListener.class);

	@EventListener
	public void onConnect(SessionConnectEvent event) {
		StompHeaderAccessor stomp = StompHeaderAccessor.wrap(event.getMessage());
		log.info("[STOMP {}] CONNECT reçu (accept-version={}, host={})",
				stomp.getSessionId(), stomp.getFirstNativeHeader("accept-version"), stomp.getHost());
	}

	@EventListener
	public void onConnected(SessionConnectedEvent event) {
		StompHeaderAccessor stomp = StompHeaderAccessor.wrap(event.getMessage());
		log.info("[STOMP {}] CONNECTED renvoyé au client : session STOMP ouverte", stomp.getSessionId());
	}

	@EventListener
	public void onSubscribe(SessionSubscribeEvent event) {
		StompHeaderAccessor stomp = StompHeaderAccessor.wrap(event.getMessage());
		log.info("[STOMP {}] SUBSCRIBE à {} (id={})",
				stomp.getSessionId(), stomp.getDestination(), stomp.getSubscriptionId());
	}

	@EventListener
	public void onDisconnect(SessionDisconnectEvent event) {
		log.info("[STOMP {}] Déconnexion ({})", event.getSessionId(), event.getCloseStatus());
	}

}
