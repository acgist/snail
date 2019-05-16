package com.acgist.main;

import java.util.List;

import org.junit.Test;

import com.acgist.snail.system.bcode.BCodeEncoder;

public class BCodeEncoderTest {

	@Test
	public void test() {
		BCodeEncoder encoder = BCodeEncoder.newInstance();
		encoder.build(List.of(1, 2, 3));
		System.out.println(encoder.toString());
	}
	
}
