package com.acgist.snail;

import java.time.Duration;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import org.junit.Test;

import com.acgist.snail.system.context.SystemThreadContext;
import com.acgist.snail.utils.ThreadUtils;

public class ThreadTest {
	
	@Test
	public void semaphore() throws InterruptedException {
		Semaphore semaphore = new Semaphore(2);
		var pool = Executors.newFixedThreadPool(200);
		for (int i = 0; i < 10; i++) {
			pool.submit(() -> {
				try {
					semaphore.acquire();
					System.out.println("++++");
					ThreadUtils.sleep(1000);
				} catch (InterruptedException e) {
					e.printStackTrace();
				} finally {
//					semaphore.release();
				}
			});
		}
		pool.submit(() -> {
			while(true) {
				ThreadUtils.sleep(1000);
				semaphore.release();
			}
		});
		ThreadUtils.sleep(Long.MAX_VALUE);
	}

	@Test
	public void lock() {
		// lock必须是同一个线程获取同一个线程释放
		Lock lock = new ReentrantLock();
		var pool = Executors.newFixedThreadPool(200);
		for (int i = 0; i < 10; i++) {
			pool.submit(() -> {
				try {
					lock.lock();
					System.out.println("++++");
					ThreadUtils.sleep(1000);
				} finally {
//					lock.unlock();
				}
			});
		}
		pool.submit(() -> {
			while(true) {
				ThreadUtils.sleep(1000);
				lock.unlock();
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
			Object lock = new Object();
			synchronized (lock) {
				ThreadUtils.wait(lock, Duration.ofSeconds(sleep));
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
		var executor = SystemThreadContext.newTimerExecutor(10, "test");
		var task = executor.scheduleAtFixedRate(() -> {
			System.out.println("----");
		}, 0, 2, TimeUnit.SECONDS);
		ThreadUtils.sleep(4L);
		task.cancel(true);
		ThreadUtils.sleep(Long.MAX_VALUE);
	}
	
}
