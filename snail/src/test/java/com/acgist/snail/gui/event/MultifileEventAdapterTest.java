package com.acgist.snail.gui.event;

import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import com.acgist.snail.context.ITaskSession;
import com.acgist.snail.context.entity.TaskEntity;
import com.acgist.snail.context.session.TaskSession;
import com.acgist.snail.gui.GuiContext;
import com.acgist.snail.gui.event.adapter.MultifileEventAdapter;
import com.acgist.snail.logger.Level;
import com.acgist.snail.logger.LoggerConfig;
import com.acgist.snail.net.DownloadException;
import com.acgist.snail.utils.Performance;

class MultifileEventAdapterTest extends Performance {

    @Test
    void testEvent() throws DownloadException {
        LoggerConfig.setLevel(Level.INFO);
        final TaskEntity taskEntity = new TaskEntity();
        taskEntity.setTorrent("D:\\tmp\\torrent\\ebfc2cf2ce69ba2f7aea36bbef290f0cce21386c.torrent");
        final ITaskSession taskSession = TaskSession.newInstance(taskEntity);
        final MultifileEventAdapter adapter = new MultifileEventAdapter();
        GuiContext.register(adapter);
        MultifileEventAdapter.files("le");
        final long a = System.currentTimeMillis();
        GuiContext.getInstance().multifile(taskSession);
        final long z = System.currentTimeMillis();
        assertTrue(z - a < 100);
    }
    
}
