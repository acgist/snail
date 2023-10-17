package com.acgist.snail.protocol.hls;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

import com.acgist.snail.context.ITaskSession;
import com.acgist.snail.net.DownloadException;
import com.acgist.snail.utils.Performance;

class HlsProtocolTest extends Performance {

    @Test
    void testName() throws DownloadException {
        final ITaskSession taskSession = HlsProtocol.getInstance().buildTaskSession("https://cdn.wls911.com:777/0f3ef709/index.m3u8");
        assertEquals("0f3ef709", taskSession.getName());
        this.log(taskSession.getFile());
    }
    
}
