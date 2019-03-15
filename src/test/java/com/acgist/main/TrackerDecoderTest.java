package com.acgist.main;

import org.junit.Test;

import com.acgist.snail.net.tracker.TrackerCoder;

public class TrackerDecoderTest {

	@Test
	public void test() {
		TrackerCoder decoder = TrackerCoder.newInstance();
		decoder.decode("d8:intervali1800e12:min intervali600e5:peers0:e");
//		decoder.decode("d14:failure reason63:Requested download is not authorized for use with this tracker.e");
		System.out.println(decoder.data());
	}
	
}
