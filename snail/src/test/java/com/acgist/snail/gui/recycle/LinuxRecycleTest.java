package com.acgist.snail.gui.recycle;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.file.Paths;

import org.junit.jupiter.api.Test;

import com.acgist.snail.gui.recycle.local.LinuxRecycle;
import com.acgist.snail.utils.Performance;

public class LinuxRecycleTest extends Performance {

    @Test
    public void testDelete() throws IOException {
        final String path = "D:/tmp/linux";
        assertTrue(Paths.get(path).toFile().createNewFile());
        final LinuxRecycle linuxRecycle = new LinuxRecycle(path);
        assertTrue(linuxRecycle.delete());
    }
    
}
