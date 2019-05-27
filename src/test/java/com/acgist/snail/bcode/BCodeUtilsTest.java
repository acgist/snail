package com.acgist.snail.bcode;

import java.io.ByteArrayInputStream;
import java.io.IOException;

import org.junit.Test;

import com.acgist.snail.system.bcode.BCodeDecoder;

public class BCodeUtilsTest {

	@Test
	public void test() throws IOException {
//		String path = "file:///e:/tmp/11e38a5270e15c60534ca48977b7d77a3c4f6340.torrent";
//		var input = new ByteArrayInputStream(Files.readAllBytes(Paths.get(URI.create(path))));
//		{"key1":"val1","key2":100,"key3":["item1",-100,["item1",-100],{"key1":"val1","key2":100}]}
//		var input = new ByteArrayInputStream("d4:key14:val14:key2i100e4:key3l5:item1i-100el5:item1i-100eed4:key14:val14:key2i100eeee".getBytes());
//		var input = new ByteArrayInputStream("d1:ei0e1:md11:ut_metadatai1e6:ut_pexi2ee13:metadata_sizei37541e1:pi4444e4:reqqi50e1:v13:BitComet 1.556:yourip4:·s;e".getBytes());
		var input = new ByteArrayInputStream("d2:ip6:  I1:rd2:id20:{HyRHߌU,~M1:pi18888ee1:t2: 1:v4:LT1:y1:re".getBytes());
		final BCodeDecoder decoder = BCodeDecoder.newInstance(input.readAllBytes());
		while (decoder.more()) {
			var data = decoder.nextMap();
			System.out.println(data);
		}
	}
	
}
