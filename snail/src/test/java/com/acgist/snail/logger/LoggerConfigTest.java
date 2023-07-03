package com.acgist.snail.logger;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

import org.junit.jupiter.api.Test;

import com.acgist.snail.utils.Performance;

class LoggerConfigTest extends Performance {

    @Test
    void testLoggerConfig() {
        assertDoesNotThrow(() -> this.log("测试"));
        LoggerConfig.off();
        assertDoesNotThrow(() -> this.log("测试"));
    }
    
}
