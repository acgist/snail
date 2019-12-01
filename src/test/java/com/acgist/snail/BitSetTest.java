package com.acgist.snail;

import java.util.BitSet;

import org.junit.Test;

import com.acgist.snail.utils.ThreadUtils;

public class BitSetTest extends BaseTest {

	@Test
	public void test() {
		BitSet set = new BitSet();
		set.set(60);
		this.log(set.size());
		this.log(set.length());
		this.log(set.cardinality());
	}
	
	@Test
	public void testCost() {
		ThreadUtils.sleep(2000);
		this.cost();
		BitSet or = new BitSet();
		BitSet index = new BitSet();
		for (int i = 0; i < 100000; i++) {
			index.set(i);
			or.or(index);
		}
		this.costed();
	}
	
}
