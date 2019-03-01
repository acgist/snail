package com.acgist.snail.service;

import com.acgist.snail.pojo.wrapper.TaskWrapper;
import com.acgist.snail.repository.impl.TaskRepository;
import com.acgist.snail.utils.FileUtils;

public class TaskService {

	private TaskRepository repository = new TaskRepository();
	
	public void delete(TaskWrapper wrapper) {
		// 删除文件：注意不删除种子文件，下载时已经将种子文件拷贝到下载目录了
		FileUtils.delete(wrapper.getFile());
		// 删除任务
		repository.delete(wrapper.getId());
	}
	
}
