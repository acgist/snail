package com.acgist.snail.utils;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;

import com.acgist.snail.BaseTest;
import com.acgist.snail.pojo.entity.ConfigEntity;

public class ObjectUtilsTest extends BaseTest {

	@Test
	public void testToString() {
		List<String> list = new ArrayList<>();
		list.add("1");
		list.add("2");
		this.log(ObjectUtils.toString(list));
		ConfigEntity config = new ConfigEntity();
		config.setId("1234");
		config.setName("test");
		this.log(ObjectUtils.toString(config));
		this.log(ObjectUtils.toString(config, config.getId(), config.getName()));
	}
	
	@Test
	public void testHashCode() {
		byte[] bytes = new byte[] {1, 2, 3};
		this.log(ObjectUtils.hashCode(bytes));
	}
	
}
