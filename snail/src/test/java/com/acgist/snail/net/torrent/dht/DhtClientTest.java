package com.acgist.snail.net.torrent.dht;

import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.Test;

import com.acgist.snail.net.DownloadException;
import com.acgist.snail.net.torrent.InfoHash;
import com.acgist.snail.utils.Performance;
import com.acgist.snail.utils.ThreadUtils;

class DhtClientTest extends Performance {

    // 本地FDM测试节点
//  private static final int PORT = 49160;
//  private static final String HOST = "127.0.0.1";
    // 本地DHT测试
//  private static final int PORT = 18888;
//  private static final String HOST = "127.0.0.1";
    // DHT节点
//  private static final int PORT = 6881;
//  private static final String HOST = "router.utorrent.com";
//  private static final int PORT = 6881;
//  private static final String HOST = "router.bittorrent.com";
    private static final int PORT = 6881;
    private static final String HOST = "dht.transmissionbt.com";
    // 种子HASH
    private static final String HASH = "5E5324691812EAA0032EA76E813CCFC4D04E7E9E";
    
    @Test
    void testPing() {
        final var client = DhtClient.newInstance(HOST, PORT);
        final var node = client.ping();
        this.log("节点信息：{}", node);
        assertNotNull(node);
    }
    
    @Test
    void testFindNode() {
        final int size = NodeContext.getInstance().nodes().size();
        final var client = DhtClient.newInstance(HOST, PORT);
        while(true) {
            client.findNode(HASH);
            ThreadUtils.sleep(1000);
            if(size != NodeContext.getInstance().nodes().size()) {
                break;
            }
        }
        assertNotEquals(size, NodeContext.getInstance().nodes().size());
    }
    
    @Test
    void testGetPeers() throws DownloadException {
        final int size = NodeContext.getInstance().nodes().size();
        final var client = DhtClient.newInstance(HOST, PORT);
        final var infoHash = InfoHash.newInstance(HASH);
        while(true) {
            client.getPeers(infoHash);
            ThreadUtils.sleep(1000);
            if(size != NodeContext.getInstance().nodes().size()) {
                break;
            }
        }
        assertNotEquals(size, NodeContext.getInstance().nodes().size());
    }
    
    @Test
    void testAnnouncePeer() throws DownloadException {
        final var client = DhtClient.newInstance(HOST, PORT);
        final var infoHash = InfoHash.newInstance(HASH);
        client.announcePeer("1".repeat(20).getBytes(), infoHash);
        ThreadUtils.sleep(1000);
        assertNotNull(client);
        assertNotNull(infoHash);
    }
    
}
