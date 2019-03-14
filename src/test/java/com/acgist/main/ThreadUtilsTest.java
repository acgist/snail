package com.acgist.main;

import org.junit.Test;

import com.acgist.snail.utils.ThreadUtils;

public class ThreadUtilsTest {

	private String name;
	
	@Test
	public void test() {
		ThreadUtilsTest t = new ThreadUtilsTest();
		System.out.println(System.currentTimeMillis());
		new Thread(() -> {
			ThreadUtils.sleep(10000);
			t.name = "xxxx";
		}).start();
		ThreadUtils.timeout(5000, () -> {
//			System.out.println("----" + System.currentTimeMillis());
			return t.name != null;
		});
		System.out.println(System.currentTimeMillis());
	}
	
}
