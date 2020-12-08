package com.acgist.snail.config;

import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.Test;

import com.acgist.snail.BaseTest;

public class DatabaseConfigTest extends BaseTest {

	@Test
	public void test() {
		final DatabaseConfig config = DatabaseConfig.getInstance();
		this.log("数据库配置：{}", DatabaseConfig.getUrl());
		assertNotNull(config);
	}
	
}
