package com.acgist.main;

import org.junit.Test;

import com.acgist.snail.coder.tracker.TrackerDecoder;

public class TrackerDecoderTest {

	@Test
	public void test() {
		TrackerDecoder decoder = TrackerDecoder.newInstance();
		decoder.decode("d8:intervali1800e12:min intervali600e5:peers0:e");
//		decoder.decode("d14:failure reason63:Requested download is not authorized for use with this tracker.e");
		System.out.println(decoder.data());
	}
	
}
