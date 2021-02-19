package com.acgist.snail.config;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

import com.acgist.snail.config.StunConfig.MessageType;
import com.acgist.snail.config.StunConfig.MethodType;
import com.acgist.snail.utils.NetUtils;
import com.acgist.snail.utils.Performance;

public class StunConfigTest extends Performance {

	@Test
	public void testMessageType() {
		short value = StunConfig.MessageType.REQUEST.of(MethodType.BINDING);
		this.log("消息类型：{}-{}", String.format("%016d", Integer.valueOf(Integer.toBinaryString(value))), value);
		assertEquals(MessageType.REQUEST, StunConfig.MessageType.of(value));
		value = StunConfig.MessageType.INDICATION.of(MethodType.BINDING);
		this.log("消息类型：{}-{}", String.format("%016d", Integer.valueOf(Integer.toBinaryString(value))), value);
		assertEquals(MessageType.INDICATION, StunConfig.MessageType.of(value));
		value = StunConfig.MessageType.RESPONSE_SUCCESS.of(MethodType.BINDING);
		this.log("消息类型：{}-{}", String.format("%016d", Integer.valueOf(Integer.toBinaryString(value))), value);
		assertEquals(MessageType.RESPONSE_SUCCESS, StunConfig.MessageType.of(value));
		value = StunConfig.MessageType.RESPONSE_ERROR.of(MethodType.BINDING);
		this.log("消息类型：{}-{}", String.format("%016d", Integer.valueOf(Integer.toBinaryString(value))), value);
		assertEquals(MessageType.RESPONSE_ERROR, StunConfig.MessageType.of(value));
	}

	@Test
	public void testXOR() {
		final short port = 4938;
		final int realPort = port ^ (StunConfig.MAGIC_COOKIE >> 16);
		this.log("真实端口：{}", realPort);
		assertEquals(12888, realPort);
		final int ip = -1777019015;
		final int realIP = ip ^ StunConfig.MAGIC_COOKIE;
		this.log("真实IP：{}-{}", realIP, NetUtils.intToIP(realIP));
		assertEquals(-1224314053, realIP);
	}

}
