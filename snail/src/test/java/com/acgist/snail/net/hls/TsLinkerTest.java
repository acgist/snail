package com.acgist.snail.net.hls;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Collectors;

import javax.crypto.Cipher;

import org.junit.jupiter.api.Test;

import com.acgist.snail.net.DownloadException;
import com.acgist.snail.utils.FileUtils;
import com.acgist.snail.utils.Performance;

class TsLinkerTest extends Performance {

    @Test
    void testLink() throws DownloadException {
        final String name = "测试";
        final String filePath = "D:\\tmp\\m3u8";
        final File parent = new File(filePath);
        final var links = List.of(parent.listFiles()).stream()
            .map(file -> file.getAbsolutePath())
            .filter(path -> !path.contains(name)) // 排除生成文件：防止重复读写
            .collect(Collectors.toList());
        Cipher cipher = null;
//      final var builder = M3u8Builder.newInstance(new File("E://snail/index.m3u8"), "https://www.acgist.com/a/b?v=1234");
//      cipher = builder.build().getCipher();
        final TsLinker linker = TsLinker.newInstance(
            name,
            filePath,
            cipher,
            links
        );
        linker.link();
        File file = Paths.get(filePath, "测试.ts").toFile();
        assertTrue(file.exists());
        FileUtils.delete(file);
    }
    
}
