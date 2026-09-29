package com.example.demo_websocket.config;

import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.http.server.ServletServerHttpResponse;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.HandshakeInterceptor;

/**
 * Trace la poignée de main (handshake) qui ouvre la connexion WebSocket.
 * <p>
 * Le handshake est une requête HTTP GET ordinaire portant l'en-tête
 * {@code Upgrade: websocket}. Si le serveur l'accepte, il répond
 * {@code 101 Switching Protocols} et la connexion TCP devient un canal WebSocket.
 */
public class HandshakeLoggingInterceptor implements HandshakeInterceptor {

	private static final Logger log = LoggerFactory.getLogger(HandshakeLoggingInterceptor.class);

	@Override
	public boolean beforeHandshake(ServerHttpRequest request, ServerHttpResponse response,
			WebSocketHandler wsHandler, Map<String, Object> attributes) {
		HttpHeaders headers = request.getHeaders();
		log.info("""
				>>> HANDSHAKE demandé : {} {} depuis {}
				      Upgrade                : {}
				      Connection             : {}
				      Sec-WebSocket-Key      : {}
				      Sec-WebSocket-Version  : {}
				      Sec-WebSocket-Protocol : {}
				      Origin                 : {}
				      User-Agent             : {}""",
				request.getMethod(), request.getURI(), request.getRemoteAddress(),
				headers.getFirst(HttpHeaders.UPGRADE),
				headers.getFirst(HttpHeaders.CONNECTION),
				headers.getFirst("Sec-WebSocket-Key"),
				headers.getFirst("Sec-WebSocket-Version"),
				headers.getFirst("Sec-WebSocket-Protocol"),
				headers.getFirst(HttpHeaders.ORIGIN),
				headers.getFirst(HttpHeaders.USER_AGENT));
		return true; // true = on laisse la connexion s'établir
	}

	@Override
	public void afterHandshake(ServerHttpRequest request, ServerHttpResponse response,
			WebSocketHandler wsHandler, Exception exception) {
		if (exception != null) {
			log.error("<<< HANDSHAKE échoué pour {}", request.getURI(), exception);
		}
		else if (response instanceof ServletServerHttpResponse servletResponse) {
			log.info("<<< HANDSHAKE terminé : HTTP {}", servletResponse.getServletResponse().getStatus());
		}
	}

}
