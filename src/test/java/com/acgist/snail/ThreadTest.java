package com.acgist.snail;

import java.time.Duration;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.junit.Test;

import com.acgist.snail.utils.ThreadUtils;

public class ThreadTest {

	@Test
	public void test() {
		long sleep = 1000000;
		ExecutorService exe = Executors.newFixedThreadPool(2);
		exe.submit(() -> {
			System.out.println("1");
			ThreadUtils.sleep(sleep);
		});
		exe.submit(() -> {
			System.out.println("2");
			ThreadUtils.wait(this, Duration.ofSeconds(sleep));
		});
		exe.submit(() -> {
			System.out.println("3");
			ThreadUtils.sleep(sleep);
		});
		ThreadUtils.sleep(Long.MAX_VALUE);
	}
	
}
