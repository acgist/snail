package com.acgist.snail.net.torrent;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import com.acgist.snail.net.DownloadException;
import com.acgist.snail.utils.Performance;

class TorrentContextTest extends Performance {

    @Test
    void testTorrentContext() throws DownloadException {
        final String path = "D:/tmp/snail/ebfc2cf2ce69ba2f7aea36bbef290f0cce21386c.torrent";
        final TorrentContext context = TorrentContext.getInstance();
        final TorrentSession a = context.newTorrentSession(path);
        context.newTorrentSession(path);
        assertNotNull(a);
        final TorrentSession b = context.newTorrentSession("0000000000000000000000000000000000000000", null);
        context.newTorrentSession("0000000000000000000000000000000000000000", null);
        assertNotNull(b);
        assertEquals(2, context.allInfoHash().size());
        assertEquals(2, context.allTorrentSession().size());
        assertTrue(context.exist("ebfc2cf2ce69ba2f7aea36bbef290f0cce21386c".toLowerCase()));
        assertTrue(context.exist("0000000000000000000000000000000000000000"));
        assertNotNull(context.torrentSession("ebfc2cf2ce69ba2f7aea36bbef290f0cce21386c".toLowerCase()));
        assertNotNull(context.torrentSession("0000000000000000000000000000000000000000"));
    }
    
}
