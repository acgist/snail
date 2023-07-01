package com.acgist.snail.gui.recycle;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.io.IOException;

import org.junit.jupiter.api.Test;

import com.acgist.snail.utils.Performance;

class RecycleContextTest extends Performance {

    @Test
    void testRecycleContext() {
        assertNotNull(RecycleContext.newInstance("D://tmp/tmp.txt"));
    }
    
    @Test
    void testRecycle() throws IOException {
        final String path = "D://tmp/" + System.currentTimeMillis() + ".txt";
        final File file = new File(path);
        file.createNewFile();
        assertTrue(file.exists());
        RecycleContext.recycle(path);
        assertFalse(file.exists());
    }
    
}
