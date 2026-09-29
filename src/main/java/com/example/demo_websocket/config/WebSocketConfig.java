package com.example.demo_websocket.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketTransportRegistration;

/**
 * Active WebSocket + STOMP et le courtier de messages intégré.
 */
@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

	/**
	 * Point d'entrée de la connexion : ws://localhost:8080/ws-chat
	 */
	@Override
	public void registerStompEndpoints(StompEndpointRegistry registry) {
		registry.addEndpoint("/ws-chat")
				.setAllowedOriginPatterns("*")                     // accepte Postman, un navigateur, etc. (dev uniquement)
				.addInterceptors(new HandshakeLoggingInterceptor()); // trace la poignée de main HTTP -> WebSocket
	}

	@Override
	public void configureMessageBroker(MessageBrokerRegistry registry) {
		// Destinations gérées par le courtier : les clients s'y abonnent (SUBSCRIBE)
		registry.enableSimpleBroker("/topic");
		// Destinations routées vers les méthodes @MessageMapping des contrôleurs (SEND)
		registry.setApplicationDestinationPrefixes("/app");
	}

	@Override
	public void configureWebSocketTransport(WebSocketTransportRegistration registration) {
		// Trace chaque trame brute reçue et complète les trames STOMP tapées dans Postman
		registration.addDecoratorFactory(RawFrameLoggingDecorator::new);
	}

}
