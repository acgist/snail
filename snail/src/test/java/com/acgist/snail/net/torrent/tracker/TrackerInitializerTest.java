package com.acgist.snail.net.torrent.tracker;

import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import com.acgist.snail.logger.LoggerConfig;
import com.acgist.snail.utils.Performance;

class TrackerInitializerTest extends Performance {

    @Test
    void testTrackerInitializer() {
        TrackerInitializer.newInstance().sync();
        assertTrue(TrackerContext.getInstance().sessions().size() > 0);
    }
    
    @Test
    void testCosted() {
        LoggerConfig.off();
        final long costed = this.costed(100000, () -> TrackerInitializer.newInstance().sync());
        assertTrue(costed < 3000);
    }
    
}
