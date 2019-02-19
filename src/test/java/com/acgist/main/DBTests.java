package com.acgist.main;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import com.acgist.killer.pojo.entity.TaskEntity;
import com.acgist.killer.repository.impl.TaskRepository;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = AcgistKillerApplication.class)
public class DBTests {

	@Autowired
	private TaskRepository taskRepository;
	
	@Test
	public void test() {
		TaskEntity task = new TaskEntity();
		task.setName("测试");
		taskRepository.save(task);
		taskRepository.findAll().forEach(entity -> {
			System.out.println(task.getName());
		});
	}
	
}
