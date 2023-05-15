package com.acgist.snail.context.entity;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class TaskEntityTest {

    @Test
    void testTaskEntity() {
        final TaskEntity source = new TaskEntity();
        final TaskEntity target = new TaskEntity();
        assertEquals(source, target);
    }
    
}
