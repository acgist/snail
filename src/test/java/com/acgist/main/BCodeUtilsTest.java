package com.acgist.main;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Paths;

import org.junit.Test;

import com.acgist.snail.utils.BCodeUtils;
import com.acgist.snail.utils.JsonUtils;

public class BCodeUtilsTest {

	@Test
	public void test() throws IOException {
		String path = "file:///e:/tmp/11e38a5270e15c60534ca48977b7d77a3c4f6340.torrent";
		var input = new ByteArrayInputStream(Files.readAllBytes(Paths.get(URI.create(path))));
//		{"key1":"val1","key2":100,"key3":["item1",-100,["item1",-100],{"key1":"val1","key2":100}]}
//		var input = new ByteArrayInputStream("d4:key14:val14:key2i100e4:key3l5:item1i-100el5:item1i-100eed4:key14:val14:key2i100eeee".getBytes());
		input.read();
		var data = BCodeUtils.d(input);
		System.out.println(JsonUtils.toJson(data));
	}
	
}
