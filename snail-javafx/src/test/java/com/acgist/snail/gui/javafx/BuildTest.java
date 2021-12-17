package com.acgist.snail.gui.javafx;

import java.util.stream.Stream;

import org.junit.jupiter.api.Test;

import com.acgist.snail.utils.Performance;

public class BuildTest extends Performance {

	@Test
	public void testBuild() {
		this.print("https://www.acgist.com");
		this.print("https://www.acgist.com    https://www.baidu.com");
		this.print("D:\\snail\\acgist.torrent D:\\bai du\\baidu.torrent");
	}
	
	private void print(String url) {
		this.log("----");
		Stream.of(url.split("\\s+")).forEach(this::log);
	}
	
}
