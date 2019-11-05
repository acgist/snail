package com.acgist.snail;

import java.util.BitSet;

import org.junit.Test;

import com.acgist.snail.utils.ThreadUtils;

public class BitSetTest {

	@Test
	public void test() {
		BitSet set = new BitSet();
		set.set(60);
		System.out.println(set.size());
		System.out.println(set.length());
		System.out.println(set.cardinality());
	}
	
	@Test
	public void cos() {
		ThreadUtils.sleep(2000);
		long begin = System.currentTimeMillis();
		BitSet or = new BitSet();
		BitSet index = new BitSet();
		for (int i = 0; i < 100000; i++) {
			index.set(i);
			or.or(index);
		}
		System.out.println(System.currentTimeMillis() - begin);
	}
	
}
