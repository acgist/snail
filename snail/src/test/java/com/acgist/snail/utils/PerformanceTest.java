package com.acgist.snail.utils;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

import org.junit.jupiter.api.Test;

class PerformanceTest extends Performance {

    @Test
    void testLog() {
        assertDoesNotThrow(() -> {
            this.log(null);
            this.log("1");
            this.log("{}", "1");
            this.log("{}", null, null);
            this.log("{}", new Object[] { null });
            this.log("{}", new Object[] { null, null });
            this.log("{}", new Object[] {});
            this.log("{}", "1", "2", 3);
        });
    }
    
}
