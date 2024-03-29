package com.acgist.snail.utils;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

import org.junit.jupiter.api.Test;

import com.acgist.snail.context.ITaskSession.FileType;

class FileUtilsTest extends Performance {
    
    @Test
    void testSystemSeparator() {
        final String truePath = "D:\\\\snail\\tmp\\\\tmp\\\\tmp";
        final String path     = FileUtils.systemSeparator("D://snail\\tmp//tmp\\\\tmp");
        assertEquals(truePath, path);
    }
    
    @Test
    void testDelete() throws IOException {
        final String path = "D://tmp/snail/" + System.currentTimeMillis();
        final File file = new File(path);
        this.log(file.getAbsolutePath());
        assertFalse(file.exists());
        Files.createFile(file.toPath());
        assertTrue(file.exists());
        FileUtils.delete(path);
        assertFalse(file.exists());
    }
    
    @Test
    void testFileName() {
        assertEquals("", FileUtils.fileName("https://www.acgist.com/demo/weixin/ ?xx?xx"));
        assertEquals("view", FileUtils.fileName("https://www.acgist.com/demo/weixin/view?xx?xx"));
        assertEquals("view", FileUtils.fileName("https://www.acgist.com/demo/weixin/view#test"));
        assertEquals("订单fds.mpe", FileUtils.fileName("%e8%ae%a2%e5%8d%95fds.mpe?xx"));
        assertEquals("订单fds.mpe", FileUtils.fileName("http://acgist/%e8%ae%a2%e5%8d%95fds.mpe?xx"));
    }
    
    @Test
    void testFileType() {
        assertEquals(FileType.VIDEO,   FileUtils.fileType("http://casd/fds.mp4"));
        assertEquals(FileType.IMAGE,   FileUtils.fileType("http://casd/fds.JPEG"));
        assertEquals(FileType.IMAGE,   FileUtils.fileType("http://casd/fds.jPg"));
        assertEquals(FileType.UNKNOWN, FileUtils.fileType("http://casd/fds"));
        assertEquals(FileType.UNKNOWN, FileUtils.fileType("http://casd/fds.acgist"));
    }
    
    @Test
    void testMoveCopy() {
        final String copy = "D:/tmp/snail/" + System.currentTimeMillis() + "/acgist.torrent";
        final String move = "D:/tmp/snail/" + System.currentTimeMillis() + "/acgist.move.torrent";
        FileUtils.copy("D:/tmp/snail/0B156834B59B0FF64EE0C9305D4D6EDE421196E6.torrent", copy);
        assertTrue(new File(copy).exists());
        FileUtils.move(copy, move);
        assertTrue(new File(move).exists());
        FileUtils.delete(new File(move).getParent());
    }
    
    @Test
    void testFile() {
        assertEquals("D:\\tmp\\snail\\acgist", FileUtils.file("D:\\tmp\\snail", "acgist"));
        assertEquals("D:\\tmp\\snail\\acgist", FileUtils.file("D:\\tmp\\snail\\", "acgist"));
        assertNotEquals(new File("tmp\\readme.md").getAbsolutePath(), FileUtils.file("tmp", "readme.md"));
        this.log(FileUtils.file("tmp", "readme.md"));
        this.log(new File("tmp\\readme.md").getAbsolutePath());
    }
    
    @Test
    void testFormatSize() {
        assertEquals("131.39KB", FileUtils.formatSize(134542L));
        assertEquals("1.00KB", FileUtils.formatSize(1024L));
        assertEquals("1.00M", FileUtils.formatSize(1024L * 1024));
        assertEquals("1024.00KB", FileUtils.formatSize(1024L * 1024 - 1));
        assertEquals("8388608.00T", FileUtils.formatSize(Long.MAX_VALUE));
        this.costed(100000, () -> FileUtils.formatSize(1024L * 1024 - 1));
    }
    
    @Test
    void testFormatSizeMB() {
        assertEquals(0.13, FileUtils.formatSizeMB(134542L));
        assertEquals(0.0, FileUtils.formatSizeMB(1024L));
        assertEquals(1.0, FileUtils.formatSizeMB(1024L * 1024));
        assertEquals(1.0, FileUtils.formatSizeMB(1024L * 1024 - 1));
    }
    
    @Test
    void testFileSize() {
        assertDoesNotThrow(() -> {
            final long size = FileUtils.fileSize("D:/tmp/snail");
            this.log(size);
            this.log(FileUtils.formatSize(size));
        });
    }
    
    @Test
    void testBuildFolder() throws IOException {
        final String path = "D://tmp/snail/" + System.currentTimeMillis();
        final File file = new File(path);
        this.log(file.getAbsolutePath());
        assertFalse(file.exists());
        FileUtils.buildFolder(file);
        assertTrue(file.exists());
        FileUtils.delete(path);
        assertFalse(file.exists());
    }
    
    @Test
    void testHash() {
        this.cost();
        FileUtils.md5("D:\\tmp\\FTPserver.exe");
        assertTrue(this.costed() < 1000);
        FileUtils.sha1("D:\\\\tmp\\\\FTPserver.exe");
        assertTrue(this.costed() < 1000);
    }
    
}
