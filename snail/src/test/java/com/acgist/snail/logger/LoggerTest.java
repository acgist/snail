package com.acgist.snail.logger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import com.acgist.snail.context.entity.TaskEntity;
import com.acgist.snail.net.NetException;
import com.acgist.snail.utils.Performance;

class LoggerTest extends Performance {

    @Test
    void testCosted() {
        // TODO：想想办法搞到500毫秒
        final long costed = this.costed(100000, () -> this.log("----{}----", System.currentTimeMillis()));
        assertTrue(costed < 3000);
    }
    
    @Test
    void testLevel() {
        String arga = null;
        String argb = "";
        final TaskEntity taskEntity = new TaskEntity();
        LOGGER.debug("debug：{} - {} - {} - {}", arga, argb, taskEntity);
        LOGGER.info("info：{} - {} - {} - {}", arga, argb, taskEntity);
        LOGGER.warn("warn：{} - {} - {} - {}", arga, argb, taskEntity);
        LOGGER.error("error：{} - {} - {} - {}", arga, argb, taskEntity);
        try {
            throw new NetException("错误测试");
        } catch (Exception e) {
            LOGGER.debug("debug", e);
            LOGGER.info("info", e);
            LOGGER.warn("warn", e);
            LOGGER.error("error", e);
        }
        assertNotNull(LOGGER);
    }
    
    @Test
    void testArray() {
        LOGGER.info("array：{}", new int[] {1, 2, 3});
        LOGGER.info("array：{}", new byte[] {1, 2, 3});
        LOGGER.info("array：{}", new char[] {1, 2, 3});
        LOGGER.info("array：{}", new long[] {1, 2, 3});
        LOGGER.info("array：{}", new short[] {1, 2, 3});
        LOGGER.info("array：{}", new float[] {1, 2, 3});
        LOGGER.info("array：{}", new double[] {1, 2, 3});
        LOGGER.info("array：{}", new boolean[] {true, false, true});
        LOGGER.info("array：{}-{}", new Integer[] {1, 2, 3}, "");
        assertNotNull(LOGGER);
    }
    
    @Test
    void testBuildSimpleName() {
        assertEquals("Test", LOGGER.buildSimpleName("Test"));
        assertEquals("c.a.Test", LOGGER.buildSimpleName("com.acgist.Test"));
        this.costed(100000, () -> LOGGER.buildSimpleName("com.acgist.Test"));
    }
    
}
