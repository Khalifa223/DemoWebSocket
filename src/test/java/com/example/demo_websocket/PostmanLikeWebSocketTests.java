package com.example.demo_websocket;

import static org.assertj.core.api.Assertions.assertThat;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.WebSocket;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;

/**
 * Rejoue le scénario Postman : un client WebSocket "brut" qui tape ses trames
 * STOMP à la main, sans le caractère NUL final.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class PostmanLikeWebSocketTests {

	@Value("${local.server.port}")
	private int port;

	@Test
	void connectSubscribeAndSendLikePostman() throws Exception {
		BlockingQueue<String> received = new LinkedBlockingQueue<>();
		WebSocket ws = HttpClient.newHttpClient().newWebSocketBuilder()
				.buildAsync(URI.create("ws://localhost:" + port + "/ws-chat"), new WebSocket.Listener() {

					private final StringBuilder buffer = new StringBuilder();

					@Override
					public CompletionStage<?> onText(WebSocket webSocket, CharSequence data, boolean last) {
						buffer.append(data);
						if (last) {
							received.add(buffer.toString());
							buffer.setLength(0);
						}
						webSocket.request(1);
						return null;
					}
				})
				.get(5, TimeUnit.SECONDS);

		ws.sendText("CONNECT\naccept-version:1.2\nhost:localhost\n\n", true).join();
		assertThat(received.poll(5, TimeUnit.SECONDS)).startsWith("CONNECTED");

		ws.sendText("SUBSCRIBE\nid:sub-0\ndestination:/topic/messages\n\n", true).join();
		Thread.sleep(200); // laisse le courtier enregistrer l'abonnement

		ws.sendText("SEND\ndestination:/app/chat\ncontent-type:application/json\n\n"
				+ "{\"sender\":\"Alice\",\"content\":\"Bonjour\"}", true).join();
		assertThat(received.poll(5, TimeUnit.SECONDS))
				.startsWith("MESSAGE")
				.contains("destination:/topic/messages")
				.contains("\"sender\":\"Alice\"")
				.contains("\"content\":\"Bonjour\"");

		ws.sendClose(WebSocket.NORMAL_CLOSURE, "fin").join();
	}

}
