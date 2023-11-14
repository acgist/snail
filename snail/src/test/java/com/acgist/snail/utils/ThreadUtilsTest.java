package com.acgist.snail.utils;

import static org.junit.jupiter.api.Assertions.assertNotEquals;

import org.junit.jupiter.api.Test;

class ThreadUtilsTest extends Performance {

    @Test
    void test() {
        this.log("{}", ThreadUtils.activeCount());
        assertNotEquals(0, ThreadUtils.activeCount());
    }
    
}
