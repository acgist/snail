package com.acgist.snail.pojo.session;

import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import com.acgist.snail.config.DownloadConfig;
import com.acgist.snail.context.session.LimitSession;
import com.acgist.snail.context.session.LimitSession.Type;
import com.acgist.snail.utils.Performance;

class LimitSessionTest extends Performance {

	@Test
	void testLimitSession() throws InterruptedException {
		final LimitSession session = new LimitSession(Type.UPLOAD);
		final long size = DownloadConfig.getUploadBufferByte();
		this.cost();
		final var a = new Thread(() -> {
			int value = 0;
			while(true) {
				value+=1024;
				session.limit(1024);
				if(value >= size) {
					break;
				}
			}
		});
		final var b = new Thread(() -> {
			int value = 0;
			while(true) {
				value+=1024;
				session.limit(1024);
				if(value >= size) {
					break;
				}
			}
		});
		a.start();
		b.start();
		a.join();
		b.join();
		assertTrue(this.costed() >= 1000);
	}
	
}
