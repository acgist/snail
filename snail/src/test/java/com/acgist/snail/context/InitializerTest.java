package com.acgist.snail.context;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import org.junit.jupiter.api.Test;

import com.acgist.snail.utils.Performance;

class InitializerTest extends Performance {

	@Test
	void testInitializer() {
		final AtomicBoolean init = new AtomicBoolean(false);
		new Initializer("ACGIST") {
			@Override
			protected void init() {
				init.set(true);
			}
			protected void destroyProxy() {};
		}.sync();
		assertTrue(init.get());
		final CountDownLatch countDownLatch = new CountDownLatch(3);
		new Initializer("异步没有延迟") {
			@Override
			protected void init() {
				countDownLatch.countDown();
			}
			protected void destroyProxy() {};
		}.asyn();
		new Initializer("异步零秒延迟", 0) {
			@Override
			protected void init() {
				countDownLatch.countDown();
			}
			protected void destroyProxy() {};
		}.asyn();
		new Initializer("异步两秒延迟", 2) {
			@Override
			protected void init() {
				assertEquals(1L, countDownLatch.getCount());
				countDownLatch.countDown();
			}
			protected void destroyProxy() {};
		}.asyn();
		assertDoesNotThrow(() -> countDownLatch.await(5, TimeUnit.SECONDS));
		assertEquals(0L, countDownLatch.getCount());
	}
	
}
