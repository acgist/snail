package com.acgist.snail.protocol.magnet;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.util.List;

import org.junit.jupiter.api.Test;

import com.acgist.snail.net.DownloadException;
import com.acgist.snail.net.PacketSizeException;
import com.acgist.snail.net.torrent.InfoHash;
import com.acgist.snail.net.torrent.TorrentContext;
import com.acgist.snail.net.torrent.TorrentSession;
import com.acgist.snail.net.torrent.dht.NodeContext;
import com.acgist.snail.utils.Performance;

class TorrentBuilderTest extends Performance {

    @Test
    void testBuild() throws DownloadException, PacketSizeException {
        final String path = "D:/tmp/snail/ebfc2cf2ce69ba2f7aea36bbef290f0cce21386c.torrent";
        final TorrentSession session = TorrentContext.getInstance().newTorrentSession(path);
        final InfoHash infoHash = session.infoHash();
        this.log("HASH：{}", infoHash.getInfoHashHex());
        NodeContext.getInstance().newNodeSession("12345678901234567890".getBytes(), "192.168.1.1", 18888);
        final List<String> trackers = List.of("https://www.acgist.com", "https://www.acgist.com/1", "https://www.acgist.com/2");
        final TorrentBuilder builder = TorrentBuilder.newInstance(infoHash, trackers);
        final String target = builder.buildFile("D:/tmp/snail/torrent");
        assertTrue(new File(target).exists());
    }
    
}
