package com.acgist.snail;

import org.junit.Test;

import com.acgist.snail.pojo.entity.ConfigEntity;
import com.acgist.snail.repository.impl.ConfigRepository;

public class ConfigRepositoryTest {
	
	@Test
	public void findOne() {
		ConfigRepository repository = new ConfigRepository();
		System.out.println(repository.findOne("47576f4a-fc42-480d-92de-183eb3f0de2d"));
		System.out.println(repository.findOne(ConfigEntity.PROPERTY_NAME, "xxxx"));
	}
	
	@Test
	public void save() {
		ConfigRepository repository = new ConfigRepository();
		ConfigEntity entity = new ConfigEntity();
		entity.setId("47576f4a-fc42-480d-92de-183eb3f0de2d");
		entity.setName("xxxx");
		repository.save(entity);
	}
	
	@Test
	public void update() {
		ConfigRepository repository = new ConfigRepository();
		ConfigEntity entity = new ConfigEntity();
		entity.setId("47576f4a-fc42-480d-92de-183eb3f0de2d");
		entity.setName("xxxx");
		repository.update(entity);
	}
	
	@Test
	public void delete() {
		ConfigRepository repository = new ConfigRepository();
		repository.delete("4e65eef9-35f9-441c-b049-fbc5dc6d97e4");
	}
	
	@Test
	public void findList() {
		ConfigRepository repository = new ConfigRepository();
		repository.findList("select * from tb_config").forEach(value -> {
			System.out.println(value.getId());
			System.out.println(value);
		});
	}
	
}
