package com.acgist.snail.context;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

import org.junit.jupiter.api.Test;

import com.acgist.snail.utils.Performance;

class TaskInitializerTest extends Performance {

    @Test
    void testTaskInitializer() {
        assertDoesNotThrow(() -> {
            TaskInitializer.newInstance().sync();
            TaskInitializer.newInstance().destroy();
        });
    }
    
}
