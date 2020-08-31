package com.acgist.snail;

import java.io.File;
import java.util.List;
import java.util.stream.Collectors;

import org.junit.jupiter.api.Test;

import com.acgist.snail.net.hls.bootstrap.TsLinker;
import com.acgist.snail.system.exception.DownloadException;

public class TsLinkerTest extends BaseTest {

	@Test
	public void link() throws DownloadException {
		final String name = "index";
		final File parent = new File("E:\\tmp\\ts\\test");
		final var links = List.of(parent.listFiles()).stream()
			.map(file -> file.getAbsolutePath())
			.filter(path -> !path.contains(name)) // 排除生成文件：防止重复读写
			.collect(Collectors.toList());
//		var builder = M3u8Builder.newInstance(new File("E://snail/index.m3u8"), "https://www.acgist.com/a/b?v=1234");
//		var cipher = builder.build().getCipher();
		final TsLinker linker = TsLinker.newInstance(
			name,
			"E:\\tmp\\ts\\test",
			null,
			links
		);
		linker.link();
	}
	
}
