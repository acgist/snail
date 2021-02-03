package com.acgist.snail.pojo.message;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

import org.junit.jupiter.api.Test;

import com.acgist.snail.pojo.message.ApplicationMessage.Type;
import com.acgist.snail.utils.Performance;

public class ApplicationMessageTest extends Performance {

	@Test
	public void testApplicationMessage() {
		var message = ApplicationMessage.message(Type.ALERT);
		this.log(message.toString());
		assertNotNull(message.toString());
		final var alert = ApplicationMessage.valueOf(message.toString());
		assertNull(alert.getBody());
		assertEquals(Type.ALERT, alert.getType());
		message = ApplicationMessage.response("acgist");
		final var response = ApplicationMessage.valueOf(message.toString());
		assertEquals("acgist", response.getBody());
		assertEquals(Type.RESPONSE, response.getType());
	}
	
}
