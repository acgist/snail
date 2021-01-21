package com.acgist.snail.utils;

import org.junit.jupiter.api.Test;

public class PeerUtilsTest extends Performance {

	@Test
	public void testAllowedFast() {
		int[] values = PeerUtils.allowedFast(1, "80.4.4.200", StringUtils.unhex("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa"));
		this.log("Piece索引：{}", values);
		values = PeerUtils.allowedFast(1313, "80.4.4.200", StringUtils.unhex("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa"));
		this.log("Piece索引：{}", values);
	}
	
}
