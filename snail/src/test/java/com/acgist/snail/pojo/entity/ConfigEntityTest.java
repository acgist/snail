package com.acgist.snail.pojo.entity;

import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.util.Date;

import org.junit.jupiter.api.Test;

import com.acgist.snail.utils.Performance;

public class ConfigEntityTest extends Performance {

	@Test
	public void testToString() {
		final ConfigEntity entity = new ConfigEntity();
		entity.setId("1234");
		entity.setName("name");
		entity.setValue("value");
		entity.setCreateDate(new Date());
		this.log(entity.toString());
		assertNotNull(entity.toString());
	}
	
}
