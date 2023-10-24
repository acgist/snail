package com.acgist.snail.net.torrent.tracker;

import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.util.List;

import org.junit.jupiter.api.Test;

import com.acgist.snail.net.DownloadException;
import com.acgist.snail.net.torrent.InfoHash;
import com.acgist.snail.net.torrent.TorrentSession;
import com.acgist.snail.utils.Performance;

class TrackerContextTest extends Performance {

    @Test
    void testTrackerContext() throws DownloadException {
        assertNotNull(TrackerContext.getInstance());
        final List<TrackerSession> sessions = TrackerContext.getInstance().sessions();
        for (TrackerSession session : sessions) {
            final TrackerLauncher launcher = TrackerContext.getInstance().buildTrackerLauncher(session, TorrentSession.newInstance(InfoHash.newInstance("0000000000000000000000000000000000000000"), null));
            TrackerContext.getInstance().removeTrackerLauncher(launcher.id());
        }
    }
    
    @Test
    void testError() {
        assertNotNull(TrackerContext.getInstance().sessions("acgist://www.acgist.com"));
        assertNotEquals(0, TrackerContext.getInstance().sessions("acgist://www.acgist.com").size());
    }

    @Test
    void testSessions() {
        TrackerContext.getInstance().sessions("https://www.acgist.com/tracker/announce").forEach(session -> {
            this.log(session);
            assertNotNull(session);
        });
    }
    
}
