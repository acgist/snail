package com.acgist.snail.config;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.nio.ByteBuffer;

import org.junit.jupiter.api.Test;

import com.acgist.snail.config.StunConfig.MessageType;
import com.acgist.snail.config.StunConfig.MethodType;
import com.acgist.snail.utils.ArrayUtils;
import com.acgist.snail.utils.NetUtils;
import com.acgist.snail.utils.NumberUtils;
import com.acgist.snail.utils.Performance;

class StunConfigTest extends Performance {

    @Test
    void testMessageType() {
        short value = StunConfig.MessageType.REQUEST.of(MethodType.BINDING);
        this.log("消息类型：{} - {}", String.format("%016d", Integer.valueOf(Integer.toBinaryString(value))), value);
        assertEquals(MessageType.REQUEST, StunConfig.MessageType.of(value));
        value = StunConfig.MessageType.INDICATION.of(MethodType.BINDING);
        this.log("消息类型：{} - {}", String.format("%016d", Integer.valueOf(Integer.toBinaryString(value))), value);
        assertEquals(MessageType.INDICATION, StunConfig.MessageType.of(value));
        value = StunConfig.MessageType.RESPONSE_SUCCESS.of(MethodType.BINDING);
        this.log("消息类型：{} - {}", String.format("%016d", Integer.valueOf(Integer.toBinaryString(value))), value);
        assertEquals(MessageType.RESPONSE_SUCCESS, StunConfig.MessageType.of(value));
        value = StunConfig.MessageType.RESPONSE_ERROR.of(MethodType.BINDING);
        this.log("消息类型：{} - {}", String.format("%016d", Integer.valueOf(Integer.toBinaryString(value))), value);
        assertEquals(MessageType.RESPONSE_ERROR, StunConfig.MessageType.of(value));
    }

    @Test
    void testXOR() {
        final short port = 4938;
        final int realPort = port ^ (StunConfig.MAGIC_COOKIE >> 16);
        this.log("真实端口：{}", realPort);
        assertEquals(12888, realPort);
        final int ip = -1777019015;
        final int realIP = ip ^ StunConfig.MAGIC_COOKIE;
        this.log("真实IP：{} - {}", realIP, NetUtils.intToIP(realIP));
        assertEquals(-1224314053, realIP);
//      final byte[] bytes = ArrayUtils.xor(NumberUtils.intToBytes(ip), NumberUtils.intToBytes(StunConfig.MAGIC_COOKIE));
        final byte[] bytes = ArrayUtils.xor(NumberUtils.intToBytes(ip), ByteBuffer.allocate(4).putInt(StunConfig.MAGIC_COOKIE).array());
        this.log("xor：{} - {} - {}", NumberUtils.bytesToInt(bytes), NetUtils.intToIP(NumberUtils.bytesToInt(bytes)), NetUtils.bytesToIP(bytes));
    }

}
