package com.acgist.main;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.junit.Test;

import com.acgist.snail.utils.ThreadUtils;

public class ThreadUtilsTest {

	private String name;
	
	@Test
	public void test() {
		ThreadUtilsTest t = new ThreadUtilsTest();
		System.out.println(System.currentTimeMillis());
		new Thread(() -> {
			ThreadUtils.sleep(20000);
			t.name = "xxxx";
		}).start();
		ThreadUtils.timeout(5000, () -> {
//			System.out.println("----" + System.currentTimeMillis());
			return t.name != null;
		});
		System.out.println(System.currentTimeMillis());
	}
	
	@Test
	public void testFuture() throws InterruptedException, ExecutionException, TimeoutException {
		CompletableFuture.runAsync(() -> {
			while(true) {
				System.out.println("bbbb");
				ThreadUtils.sleep(1000);
			}
		}).get(5, TimeUnit.SECONDS);
		System.out.println("xxxx");
	}
	
}
