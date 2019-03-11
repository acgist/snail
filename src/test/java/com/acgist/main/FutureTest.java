package com.acgist.main;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.FutureTask;

import org.junit.Test;

public class FutureTest {

	@Test
	public void test() throws InterruptedException, ExecutionException {
		FutureTask<Integer> future = new FutureTask<>(()-> {
			return 1;
		});
		future.run();
		int x = future.get();
		System.out.println(x);
	}
	
}
