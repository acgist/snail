package com.acgist.main;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;

import org.junit.Test;

import com.acgist.snail.utils.BCodeUtils;
import com.acgist.snail.utils.JsonUtils;
import com.acgist.snail.utils.NetUtils;

public class BCodeUtilsTest {

	@Test
	public void test() throws IOException {
//		String path = "file:///e:/tmp/11e38a5270e15c60534ca48977b7d77a3c4f6340.torrent";
//		var input = new ByteArrayInputStream(Files.readAllBytes(Paths.get(URI.create(path))));
//		{"key1":"val1","key2":100,"key3":["item1",-100,["item1",-100],{"key1":"val1","key2":100}]}
//		var input = new ByteArrayInputStream("d4:key14:val14:key2i100e4:key3l5:item1i-100el5:item1i-100eed4:key14:val14:key2i100eeee".getBytes());
//		var input = new ByteArrayInputStream("d1:ei0e1:md11:ut_metadatai1e6:ut_pexi2ee13:metadata_sizei37541e1:pi4444e4:reqqi50e1:v13:BitComet 1.556:yourip4:·s;e".getBytes());
		var input = new ByteArrayInputStream("d1:ei0e1:md11:ut_metadatai2e6:ut_pexi1ee13:metadata_sizei10424e1:pi15000e4:reqqi255e1:v20:-XL0012-Q�'�4��xe".getBytes());
		input.read();
		var data = BCodeUtils.d(input);
		System.out.println(data);
		System.out.println(JsonUtils.toJson(data));
		
		System.out.println(BCodeUtils.getString(data, "v"));
		System.out.println(new String((byte[]) data.get("v")));
		ByteBuffer ipAddress = ByteBuffer.wrap((byte[]) data.get("yourip"));
		System.out.println(NetUtils.intToIp(ipAddress.getInt()));
	}
	
}
