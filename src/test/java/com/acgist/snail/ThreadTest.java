package com.acgist.snail;

import java.time.Duration;
import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

import org.junit.Test;

import com.acgist.snail.system.context.SystemThreadContext;
import com.acgist.snail.utils.ThreadUtils;

public class ThreadTest {
	
	@Test
	public void semaphore() throws InterruptedException {
		Semaphore semaphore = new Semaphore(2);
		var pool = Executors.newFixedThreadPool(200);
		for (int i = 0; i < 10; i++) {
			semaphore.acquire();
			pool.submit(() -> {
				try {
					System.out.println("++++");
					ThreadUtils.sleep(new Random().nextInt(2000));
				} finally {
					semaphore.release();
				}
			});
		}
		System.out.println("=================================");
		pool.submit(() -> {
			while(true) {
				ThreadUtils.sleep(100);
				System.out.println("----");
			}
		});
		ThreadUtils.sleep(Long.MAX_VALUE);
	}

	@Test
	public void waitTest() {
		long sleep = 1000000;
		ExecutorService exe = Executors.newFixedThreadPool(2);
		exe.submit(() -> {
			System.out.println("1");
			ThreadUtils.sleep(sleep);
		});
		exe.submit(() -> {
			System.out.println("2");
			synchronized (this) {
				ThreadUtils.wait(this, Duration.ofSeconds(sleep));
			}
		});
		exe.submit(() -> {
			System.out.println("3");
			ThreadUtils.sleep(sleep);
		});
		ThreadUtils.sleep(Long.MAX_VALUE);
	}
	
	@Test
	public void timer() {
		var executor = SystemThreadContext.newScheduledExecutor(10, "test");
		var task = executor.scheduleAtFixedRate(() -> {
			System.out.println("----");
		}, 0, 2, TimeUnit.SECONDS);
		ThreadUtils.sleep(4L);
		task.cancel(true);
		ThreadUtils.sleep(Long.MAX_VALUE);
	}
	
}
