package com.acgist.snail.net.application;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

import org.junit.jupiter.api.Test;

import com.acgist.snail.net.application.ApplicationMessage.Type;
import com.acgist.snail.utils.Performance;

class ApplicationMessageTest extends Performance {

	@Test
	void testApplicationMessage() {
		var message = Type.ALERT.build();
		this.log(message.toString());
		assertNotNull(message.toString());
		final var alert = ApplicationMessage.valueOf(message.toString());
		assertNull(alert.getBody());
		assertEquals(Type.ALERT, alert.getType());
		message = ApplicationMessage.Type.RESPONSE.build("acgist");
		final var response = ApplicationMessage.valueOf(message.toString());
		assertEquals("acgist", response.getBody());
		assertEquals(Type.RESPONSE, response.getType());
	}
	
}
