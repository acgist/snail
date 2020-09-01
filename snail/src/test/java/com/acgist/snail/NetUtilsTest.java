package com.acgist.snail;

import org.junit.jupiter.api.Test;

import com.acgist.snail.utils.NetUtils;

public class NetUtilsTest extends BaseTest {

	@Test
	public void ip() {
		this.log(NetUtils.decodeLongToIp(2130706433));
		this.log(NetUtils.encodeIpToLong("127.1.1.1"));
	}
	
}
