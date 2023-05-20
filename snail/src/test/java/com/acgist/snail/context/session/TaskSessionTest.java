package com.acgist.snail.context.session;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.util.Map;

import org.junit.jupiter.api.Test;

import com.acgist.snail.context.ITaskSession;
import com.acgist.snail.context.ITaskSessionStatus.Status;
import com.acgist.snail.context.entity.TaskEntity;
import com.acgist.snail.net.DownloadException;
import com.acgist.snail.utils.Performance;

class TaskSessionTest extends Performance {

    @Test
    void testTaskSession() throws DownloadException {
        final TaskEntity taskEntity    = new TaskEntity();
        final ITaskSession taskSession = TaskSession.newInstance(taskEntity);
        assertFalse(taskSession.verify());
        taskSession.setStatus(Status.PAUSE);
    }

    @Test
    void testToMap() throws DownloadException {
        final TaskEntity taskEntity = new TaskEntity();
        taskEntity.setStatus(Status.AWAIT);
        final ITaskSession taskSession = TaskSession.newInstance(taskEntity);
        final Map<String, Object> map = taskSession.toMap();
        assertNotNull(map);
        this.log("{}", map);
    }
    
}
