package com.acgist.snail.net.codec;

import java.util.function.Supplier;

import org.junit.jupiter.api.Test;

import com.acgist.snail.utils.Performance;

class FunctionTest extends Performance {

	@Test
	void testCost() {
		final Integer a = 1;
		final Integer b = 2;
		this.costed(10, () -> {
			this.costed(1000000, () -> this.direct(a, b));
			this.costed(1000000, () -> this.function(() -> a, () -> b));
		});
	}
	
	int direct(Integer a, Integer b) {
		return a + b;
	}
	
	int function(Supplier<Integer> a, Supplier<Integer> b) {
		return a.get() + b.get();
	}
	
}
