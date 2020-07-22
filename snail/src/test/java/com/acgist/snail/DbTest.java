package com.acgist.snail;

import java.util.List;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import com.acgist.snail.pojo.entity.ConfigEntity;
import com.acgist.snail.pojo.wrapper.ResultSetWrapper;
import com.acgist.snail.repository.DatabaseManager;
import com.acgist.snail.repository.impl.ConfigRepository;
import com.acgist.snail.system.initializer.impl.DatabaseInitializer;

public class DbTest extends BaseTest {

	@BeforeAll
	public static final void init() {
		DatabaseInitializer.newInstance().sync();
	}
	
	@Test
	public void testSelect() {
		List<ResultSetWrapper> list = DatabaseManager.getInstance().select("select * from tb_config");
		list.forEach(value -> {
			this.log(value);
		});
	}
	
	@Test
	public void testFindOne() {
		ConfigRepository repository = new ConfigRepository();
		this.log(repository.findOne("d08e76e1-5b05-47c4-9224-eb1dd0f0c898"));
		this.log(repository.findOne(ConfigEntity.PROPERTY_NAME, "test"));
	}
	
	@Test
	public void testSave() {
		ConfigRepository repository = new ConfigRepository();
		ConfigEntity entity = new ConfigEntity();
		entity.setName("test");
		entity.setValue("test-save");
		repository.save(entity);
		this.log(entity.getId());
	}
	
	@Test
	public void testUpdate() {
		ConfigRepository repository = new ConfigRepository();
		ConfigEntity entity = new ConfigEntity();
		entity.setId("d08e76e1-5b05-47c4-9224-eb1dd0f0c898");
		entity.setName("test");
		entity.setValue("test-update");
		repository.update(entity);
	}
	
	@Test
	public void testDelete() {
		ConfigRepository repository = new ConfigRepository();
		repository.delete("225b6777-c3d5-49d7-a4bc-60fa4bb6f17c");
		repository.delete("86d1715e-3960-4f0f-8936-22d03040ef83");
	}
	
	@Test
	public void testFindList() {
		ConfigRepository repository = new ConfigRepository();
		repository.findList("select * from tb_config").forEach(value -> {
			this.log(value);
		});
	}
	
}
