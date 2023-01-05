package com.acgist.snail.context.session;

import static org.junit.jupiter.api.Assertions.assertFalse;

import org.junit.jupiter.api.Test;

import com.acgist.snail.context.ITaskSession;
import com.acgist.snail.context.ITaskSessionStatus.Status;
import com.acgist.snail.context.entity.TaskEntity;
import com.acgist.snail.net.DownloadException;

class TaskSessionTest {

	@Test
	void testTaskSession() throws DownloadException {
		final TaskEntity taskEntity = new TaskEntity();
		final ITaskSession taskSession = TaskSession.newInstance(taskEntity);
		assertFalse(taskSession.verify());
		taskSession.setStatus(Status.PAUSE);
	}
	
}
