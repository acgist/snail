package com.acgist.snail.pojo.bean;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Base64;

import org.junit.jupiter.api.Test;

import com.acgist.snail.BaseTest;
import com.acgist.snail.context.exception.DownloadException;
import com.acgist.snail.context.exception.NetException;
import com.acgist.snail.protocol.hls.bootstrap.M3u8Builder;

public class M3u8Test extends BaseTest {

//	http://ivi.bupt.edu.cn/hls/cctv6hd.m3u8
//	https://dco4urblvsasc.cloudfront.net/811/81095_ywfZjAuP/game/index.m3u8
	
	@Test
	public void build() throws DownloadException, IOException, NetException {
		var builder = M3u8Builder.newInstance(new File("E://snail/index.m3u8"), "https://www.acgist.com/a/b?v=1234");
		M3u8 m3u8 = builder.build();
		this.log(m3u8);
		builder = M3u8Builder.newInstance(Files.readString(new File("E://snail/index.m3u8").toPath()), "https://www.acgist.com/a/b?v=1234");
		m3u8 = builder.build();
		this.log(m3u8);
		builder = M3u8Builder.newInstance(new File("E://snail/cctv6hd.m3u8"), "https://www.acgist.com/a/b?v=1234");
		m3u8 = builder.build();
		this.log(m3u8);
		builder = M3u8Builder.newInstance(Files.readString(new File("E://snail/cctv6hd.m3u8").toPath()), "https://www.acgist.com/a/b?v=1234");
		m3u8 = builder.build();
		this.log(m3u8);
	}
	
	@Test
	public void testAes128() throws NetException, DownloadException {
		var builder = M3u8Builder.newInstance(new File("E://snail/index.m3u8"), "https://www.acgist.com/a/b?v=1234");
		String content = "fsdFa0eSflI14gGZKoq2pX8z/k8dioVlLCPfVRrvqe0=";
		final var cipher = builder.build().getCipher();
		final byte[] target = content.getBytes();
		this.log(target.length);
		this.log(target[target.length - 1]);
		final byte[] source = cipher.update(Base64.getDecoder().decode(content));
		this.log(source.length);
		final int padding = source[source.length - 1];
		this.log(padding);
		this.log(new String(source, 0, source.length - padding));
	}
	
}
