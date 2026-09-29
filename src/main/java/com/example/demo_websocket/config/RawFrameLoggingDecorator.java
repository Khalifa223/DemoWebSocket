package com.example.demo_websocket.config;

import java.util.concurrent.atomic.AtomicInteger;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.WebSocketMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.WebSocketHandlerDecorator;

/**
 * Se place devant le gestionnaire STOMP de Spring et voit passer chaque trame
 * WebSocket exactement telle que le client l'a envoyée.
 * <p>
 * Il sert à deux choses :
 * <ol>
 * <li>tracer l'ouverture/fermeture de la connexion et le contenu brut des trames ;</li>
 * <li>faciliter les tests avec Postman : une trame STOMP doit se terminer par le
 * caractère NUL ({@code ^@}), qu'on ne peut pas taper dans Postman. S'il manque,
 * on l'ajoute avant de transmettre la trame à Spring.</li>
 * </ol>
 */
public class RawFrameLoggingDecorator extends WebSocketHandlerDecorator {

	private static final Logger log = LoggerFactory.getLogger(RawFrameLoggingDecorator.class);

	private static final String FRAME_COUNT = "rawFrameCount";

	public RawFrameLoggingDecorator(WebSocketHandler delegate) {
		super(delegate);
	}

	@Override
	public void afterConnectionEstablished(WebSocketSession session) throws Exception {
		session.getAttributes().put(FRAME_COUNT, new AtomicInteger());
		log.info("[WS {}] Connexion WebSocket ouverte depuis {} sur {}",
				session.getId(), session.getRemoteAddress(), session.getUri());
		super.afterConnectionEstablished(session);
	}

	@Override
	public void handleMessage(WebSocketSession session, WebSocketMessage<?> message) throws Exception {
		if (message instanceof TextMessage textMessage) {
			String payload = textMessage.getPayload();
			int n = ((AtomicInteger) session.getAttributes().get(FRAME_COUNT)).incrementAndGet();
			log.info("[WS {}] Trame n°{}{} reçue ({} caractères) :\n{}",
					session.getId(), n, (n == 1 ? " (PREMIÈRE)" : ""), payload.length(),
					payload.replace("\0", "^@"));

			String frame = completeStompFrame(payload);
			if (!frame.equals(payload)) {
				log.debug("[WS {}] Trame complétée pour STOMP (ligne vide et/ou ^@ ajoutés)", session.getId());
				message = new TextMessage(frame);
			}
		}
		super.handleMessage(session, message);
	}

	@Override
	public void afterConnectionClosed(WebSocketSession session, CloseStatus closeStatus) throws Exception {
		log.info("[WS {}] Connexion WebSocket fermée : {}", session.getId(), closeStatus);
		super.afterConnectionClosed(session, closeStatus);
	}

	/**
	 * Ajoute ce qui manque à une trame STOMP tapée à la main : la ligne vide qui
	 * sépare les en-têtes du corps, et le NUL final. Une trame vide (heartbeat) ou
	 * contenant déjà un NUL est renvoyée telle quelle.
	 */
	static String completeStompFrame(String payload) {
		if (payload.isBlank() || payload.indexOf('\0') >= 0) {
			return payload;
		}
		String frame = payload;
		if (!frame.contains("\n\n") && !frame.contains("\r\n\r\n")) {
			frame = frame.stripTrailing() + "\n\n";
		}
		return frame + '\0';
	}

}
