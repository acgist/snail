package com.acgist.snail.net.torrent.utp;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.net.InetSocketAddress;

import org.junit.jupiter.api.Test;

import com.acgist.snail.config.SystemConfig;
import com.acgist.snail.config.UtpConfig;
import com.acgist.snail.net.torrent.TorrentServer;
import com.acgist.snail.net.torrent.peer.PeerSubMessageHandler;
import com.acgist.snail.utils.NetUtils;
import com.acgist.snail.utils.Performance;
import com.acgist.snail.utils.ThreadUtils;

class UtpMessageHandlerTest extends Performance {

    @Test
    void testServer() {
        assertDoesNotThrow(() -> {
            TorrentServer.getInstance();
            ThreadUtils.sleep(100000);
        });
    }
    
    @Test
    void testConnect() {
        SystemConfig.setTorrentPort(18899);
        final InetSocketAddress socketAddress = NetUtils.buildSocketAddress("127.0.0.1", 18888);
        final UtpMessageHandler handler = new UtpMessageHandler(PeerSubMessageHandler.newInstance(), socketAddress);
        handler.handle(TorrentServer.getInstance().getChannel());
        boolean connect = handler.connect();
        this.log("连接：{}", connect);
        assertTrue(connect);
        connect = handler.connect();
        this.log("连接：{}", connect);
        assertTrue(connect);
        handler.close();
        ThreadUtils.sleep(5000);
    }
    
    @Test
    void testSelect() {
        final UtpConfig.Type[] types = UtpConfig.Type.values();
        for (UtpConfig.Type type : types) {
            this.log("{} - {} - {} - {}", type, type.getType(), type.getTypeVersion(), Integer.toHexString(type.getTypeVersion()));
            assertEquals(UtpConfig.Type.of(type.getTypeVersion()), type);
        }
        this.log("{} - {} - {}", 'd', (int) 'd', Integer.toHexString('d'));
        this.log("{} - {} - {}", 'f', (int) 'f', Integer.toHexString('f'));
        this.log("{} - {} - {}", 'q', (int) 'q', Integer.toHexString('q'));
        assertEquals(0x00, UtpConfig.Type.DATA.getType());
        assertEquals(0x01, UtpConfig.Type.DATA.getTypeVersion());
        assertEquals(0x04, UtpConfig.Type.SYN.getType());
        assertEquals(0x41, UtpConfig.Type.SYN.getTypeVersion());
    }
    
}
