package com.acgist.snail.protocol.hls;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.io.File;
import java.io.IOException;

import org.junit.jupiter.api.Test;

import com.acgist.snail.net.DownloadException;
import com.acgist.snail.net.NetException;
import com.acgist.snail.net.hls.M3u8;
import com.acgist.snail.net.hls.M3u8.Type;
import com.acgist.snail.utils.Performance;

class M3u8BuilderTest extends Performance {

    @Test
    void testFile() throws DownloadException, IOException, NetException {
        final M3u8 m3u8 = M3u8Builder.newInstance(new File("D://tmp/snail/m3u8/file.m3u8"), "https://www.acgist.com/file/acgist").build();
        assertEquals(Type.FILE, m3u8.getType());
        m3u8.getLinks().forEach(this::log);
    }
    
    @Test
    void testM3U8() throws NetException, DownloadException {
        final M3u8 m3u8 = M3u8Builder.newInstance(new File("D://tmp/snail/m3u8/m3u8.m3u8"), "https://www.acgist.com/m3u8/acgist").build();
        assertEquals(Type.M3U8, m3u8.getType());
        this.log(m3u8.getMaxRateLink());
        m3u8.getLinks().forEach(this::log);
    }
    
    @Test
    void testStream() throws NetException, DownloadException {
        final M3u8 m3u8 = M3u8Builder.newInstance(new File("D://tmp/snail/m3u8/stream.m3u8"), "https://www.acgist.com/stream/acgist").build();
        assertEquals(Type.STREAM, m3u8.getType());
        m3u8.getLinks().forEach(this::log);
    }
    
    @Test
    void testCipher() throws NetException, DownloadException {
        assertThrows(NetException.class, () -> {
//          M3u8Builder.newInstance(new File("D://tmp/snail/m3u8/cipher.m3u8"),   "https://www.acgist.com/cipher/acgist").build();
            M3u8Builder.newInstance(new File("D://tmp/snail/m3u8/sequence.m3u8"), "https://www.acgist.com/cipher/acgist").build();
        });
    }
    
}
