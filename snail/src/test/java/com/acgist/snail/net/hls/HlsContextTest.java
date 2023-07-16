package com.acgist.snail.net.hls;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.Test;

import com.acgist.snail.context.ITaskSession;
import com.acgist.snail.context.entity.TaskEntity;
import com.acgist.snail.context.session.TaskSession;
import com.acgist.snail.net.DownloadException;
import com.acgist.snail.net.hls.M3u8.Type;
import com.acgist.snail.utils.Performance;

class HlsContextTest extends Performance {

    @Test
    void testHlsContext() throws DownloadException {
        final String id = UUID.randomUUID().toString();
        final TaskEntity task = new TaskEntity();
        task.setId(id);
        task.setName("acgist");
        final ITaskSession session = TaskSession.newInstance(task);
        assertDoesNotThrow(() -> {
            HlsContext.getInstance().m3u8(id, new M3u8(Type.FILE, null, List.of()));
            HlsContext.getInstance().hlsSession(session);
            HlsContext.getInstance().remove(session);
        });
    }
    
}
