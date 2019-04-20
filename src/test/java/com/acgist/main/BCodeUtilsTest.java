package com.acgist.main;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;

import org.junit.Test;

import com.acgist.snail.net.bcode.BCodeDecoder;
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
		var input = new ByteArrayInputStream("d12:complete_agoi911e1:md11:lt_donthavei7e10:share_modei8e11:upload_onlyi3e12:ut_holepunchi4e11:ut_metadatai2ee13:metadata_sizei37541e4:reqqi500e1:v10:FDM 5.1.386:yourip4:tzåe".getBytes());
		final BCodeDecoder decoder = BCodeDecoder.newInstance(input.readAllBytes());
		while (decoder.hasMore()) {
			var data = decoder.mustMap();
			System.out.println(data);
			System.out.println(JsonUtils.toJson(data));
			
			System.out.println(BCodeDecoder.getString(data, "v"));
			System.out.println(new String((byte[]) data.get("v")));
			ByteBuffer ipAddress = ByteBuffer.wrap((byte[]) data.get("yourip"));
			System.out.println(NetUtils.encodeIntToIp(ipAddress.getInt()));
		}
	}
	
}
