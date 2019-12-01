package com.acgist.snail;

import java.time.Duration;
import java.util.BitSet;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import org.junit.Test;

import com.acgist.snail.system.context.SystemThreadContext;
import com.acgist.snail.utils.ThreadUtils;

public class ThreadTest extends BaseTest {
	
	@Test
	public void semaphore() throws InterruptedException {
		Semaphore semaphore = new Semaphore(2);
		var pool = Executors.newFixedThreadPool(200);
		for (int i = 0; i < 10; i++) {
			pool.submit(() -> {
				try {
					semaphore.acquire();
					this.log("++++");
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
		this.pause();
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
					this.log("++++");
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
		this.pause();
	}
	
	@Test
	public void waitTest() {
		long sleep = 1000000;
		ExecutorService exe = Executors.newFixedThreadPool(2);
		exe.submit(() -> {
			this.log("1");
			ThreadUtils.sleep(sleep);
		});
		exe.submit(() -> {
			this.log("2");
			Object lock = new Object();
			synchronized (lock) {
				ThreadUtils.wait(lock, Duration.ofSeconds(sleep));
			}
		});
		exe.submit(() -> {
			this.log("3");
			ThreadUtils.sleep(sleep);
		});
		this.pause();
	}
	
	@Test
	public void timer() {
		var executor = SystemThreadContext.newTimerExecutor(10, "test");
		var task = executor.scheduleAtFixedRate(() -> {
			this.log("----");
		}, 0, 2, TimeUnit.SECONDS);
		ThreadUtils.sleep(4L);
		task.cancel(true);
		this.pause();
	}
	
	@Test
	public void bitSet() {
		BitSet bitSet = new BitSet();
		for (int index = 0; index < 100; index++) {
			int x = index;
			SystemThreadContext.submit(() -> {
				bitSet.set(x);
			});
		}
		this.log(bitSet.cardinality());
	}
	
}
