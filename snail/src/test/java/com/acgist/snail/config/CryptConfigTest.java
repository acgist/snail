package com.acgist.snail.config;

import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.Test;

import com.acgist.snail.BaseTest;

public class CryptConfigTest extends BaseTest {

	@Test
	public void test() {
		final CryptConfig.Strategy defaultStrategy = CryptConfig.STRATEGY;
		this.log("ARC4 provide：{}", CryptConfig.CryptAlgo.ARC4.provide());
		this.log("PLAINTEXT provide：{}", CryptConfig.CryptAlgo.PLAINTEXT.provide());
		this.log("默认级别：{}", defaultStrategy);
		this.log("是否加密：{}", defaultStrategy.crypt());
		this.log("加密模式：{}", defaultStrategy.provide());
		assertNotNull(defaultStrategy);
	}
	
}
