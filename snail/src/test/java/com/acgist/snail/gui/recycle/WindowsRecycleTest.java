package com.acgist.snail.gui.recycle;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

import org.junit.jupiter.api.Test;

import com.acgist.snail.gui.recycle.local.WindowsRecycle;
import com.acgist.snail.utils.FileUtils;
import com.acgist.snail.utils.Performance;
import com.acgist.snail.utils.StringUtils;

class WindowsRecycleTest extends Performance {

    @Test
    void testDelete() throws IOException {
        final String path = "D:/tmp/" + System.currentTimeMillis() + ".acgist";
        FileUtils.write(path, "acgist".repeat(1024).getBytes());
        final File file = new File(path);
        file.createNewFile();
        assertTrue(file.exists());
        new WindowsRecycle(path).delete();
        assertFalse(file.exists());
    }
    
    @Test
    void testDeleteRelative() {
        String path = "/test";
        FileUtils.write(path, "".getBytes());
        assertTrue(Paths.get(path).toFile().exists());
        new WindowsRecycle(path).delete();
        assertFalse(Paths.get(path).toFile().exists());
    }
    
    @Test
    void testFileInfo() throws IOException {
        final var bytes = Files.readAllBytes(Paths.get("D:/$RECYCLE.BIN/S-1-5-21-3564299411-3826043463-933790329-1001/$R12000836.acgist"));
        this.log(StringUtils.hex(bytes));
        assertNotNull(bytes);
    }
    
}
