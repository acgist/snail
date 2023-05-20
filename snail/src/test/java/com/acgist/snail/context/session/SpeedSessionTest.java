package com.acgist.snail.context.session;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import com.acgist.snail.config.SystemConfig;
import com.acgist.snail.utils.Performance;
import com.acgist.snail.utils.ThreadUtils;

class SpeedSessionTest extends Performance {

    @Test
    void testSpeedSession() {
        final SpeedSession session = new SpeedSession();
        session.buffer(1024);
        session.buffer(1024);
        ThreadUtils.sleep(SystemConfig.REFRESH_INTERVAL_MILLIS);
        session.buffer(1024);
        session.buffer(1024);
        this.log(session.getSpeed());
        assertTrue(session.getSpeed() <= 1024);
        session.reset();
        assertTrue(session.getSpeed() <= 1024);
        ThreadUtils.sleep(SystemConfig.REFRESH_INTERVAL_MILLIS);
        assertEquals(0, session.getSpeed());
    }
    
}
