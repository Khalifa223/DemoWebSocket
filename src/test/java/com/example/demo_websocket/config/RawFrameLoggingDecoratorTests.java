package com.example.demo_websocket.config;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class RawFrameLoggingDecoratorTests {

	@Test
	void addsMissingNul() {
		assertThat(RawFrameLoggingDecorator.completeStompFrame("CONNECT\naccept-version:1.2\n\n"))
				.isEqualTo("CONNECT\naccept-version:1.2\n\n\0");
	}

	@Test
	void addsMissingBlankLineAndNul() {
		assertThat(RawFrameLoggingDecorator.completeStompFrame("CONNECT\naccept-version:1.2\n"))
				.isEqualTo("CONNECT\naccept-version:1.2\n\n\0");
	}

	@Test
	void keepsBodyAsIs() {
		assertThat(RawFrameLoggingDecorator.completeStompFrame("SEND\ndestination:/app/chat\n\n{\"a\":1}"))
				.isEqualTo("SEND\ndestination:/app/chat\n\n{\"a\":1}\0");
	}

	@Test
	void leavesCompleteFramesAndHeartbeatsUntouched() {
		assertThat(RawFrameLoggingDecorator.completeStompFrame("CONNECT\n\n\0")).isEqualTo("CONNECT\n\n\0");
		assertThat(RawFrameLoggingDecorator.completeStompFrame("\n")).isEqualTo("\n");
	}

}
