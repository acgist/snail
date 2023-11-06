package com.acgist.snail.net.torrent;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.Test;

import com.acgist.snail.config.StunConfig;
import com.acgist.snail.net.stun.StunMessageHandler;
import com.acgist.snail.net.torrent.dht.DhtMessageHandler;
import com.acgist.snail.net.torrent.utp.UtpMessageHandler;
import com.acgist.snail.utils.DateUtils;
import com.acgist.snail.utils.Performance;

class TorrentAcceptHandlerTest extends Performance {

    @Test
    void testUtpStun() {
        long value = System.currentTimeMillis();
        assertEquals(value * 1000, TimeUnit.MILLISECONDS.toMicros(value));
        value = System.nanoTime();
        assertEquals(value / 1000, TimeUnit.NANOSECONDS.toMicros(value));
        this.costed(100000, () -> System.nanoTime());
        this.costed(100000, () -> System.currentTimeMillis());
        this.log("--------------------------------------------------------");
        this.costed(100000, () -> TimeUnit.NANOSECONDS.toMicros(System.nanoTime()));
        this.costed(100000, () -> TimeUnit.NANOSECONDS.toMicros(System.currentTimeMillis()));
        this.log("--------------------------------------------------------");
        this.costed(100000, () -> DateUtils.timestampUs());
        this.log(DateUtils.timestampUs());
        this.log(StunConfig.MAGIC_COOKIE);
    }
    
    @Test
    void testCosted() {
        final TorrentAcceptHandler handler = TorrentAcceptHandler.getInstance();
        final InetSocketAddress address = new InetSocketAddress(1024);
        final ByteBuffer buffer = ByteBuffer.allocate(16);
        buffer.put((byte) 'd');
        buffer.put((byte) 0x01);
        buffer.put((byte) 0x01);
        buffer.put((byte) 0x01);
        buffer.putInt(StunConfig.MAGIC_COOKIE);
        long costed = this.costed(100000, () -> {
            assertTrue(handler.messageHandler(buffer, address) instanceof DhtMessageHandler);
        });
        assertTrue(costed < 1000);
        buffer.clear();
        buffer.put((byte) 0x01);
        buffer.put((byte) 0x01);
        buffer.put((byte) 0x01);
        buffer.put((byte) 0x01);
        buffer.putInt(StunConfig.MAGIC_COOKIE);
        costed = this.costed(100000, () -> {
            assertTrue(handler.messageHandler(buffer, address) instanceof StunMessageHandler);
        });
        assertTrue(costed < 1000);
        buffer.clear();
        buffer.put((byte) 0x01);
        buffer.put((byte) 0x01);
        buffer.put((byte) 0x01);
        buffer.put((byte) 0x00);
        buffer.putInt(0);
        // 设置握手消息
        handler.messageHandler(buffer, address);
        buffer.clear();
        buffer.put((byte) 0x01);
        buffer.put((byte) 0x01);
        buffer.put((byte) 0x01);
        buffer.put((byte) 0x01);
        buffer.putInt(0);
        costed = this.costed(100000, () -> {
            assertTrue(handler.messageHandler(buffer, address) instanceof UtpMessageHandler);
        });
        assertTrue(costed < 1000);
    }
    
}
