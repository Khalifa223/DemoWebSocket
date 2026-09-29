package com.example.demo_websocket;

import static org.assertj.core.api.Assertions.assertThat;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;

/**
 * Vérifie que la page, notre script et la librairie STOMP sont bien servis.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class StaticResourcesTests {

	@Value("${local.server.port}")
	private int port;

	private final HttpClient http = HttpClient.newHttpClient();

	@Test
	void servesIndexPage() throws Exception {
		HttpResponse<String> response = get("/");
		assertThat(response.statusCode()).isEqualTo(200);
		assertThat(response.body()).contains("<title>Chat WebSocket</title>");
	}

	@Test
	void servesAppScript() throws Exception {
		HttpResponse<String> response = get("/js/app.js");
		assertThat(response.statusCode()).isEqualTo(200);
		assertThat(response.body()).contains("new StompJs.Client");
	}

	@Test
	void servesStompLibraryWithoutVersionInUrl() throws Exception {
		HttpResponse<String> response = get("/webjars/stomp__stompjs/bundles/stomp.umd.min.js");
		assertThat(response.statusCode()).isEqualTo(200);
		assertThat(response.body()).contains("StompJs");
	}

	private HttpResponse<String> get(String path) throws Exception {
		HttpRequest request = HttpRequest.newBuilder(URI.create("http://localhost:" + port + path)).build();
		return http.send(request, HttpResponse.BodyHandlers.ofString());
	}

}
