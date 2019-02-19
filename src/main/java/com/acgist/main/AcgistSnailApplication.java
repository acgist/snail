package com.acgist.main;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import com.acgist.snail.context.SpringContextUtils;
import com.acgist.snail.window.main.MainWindow;

/**
 * 启动类
 */
@EntityScan("com.acgist.snail.pojo.entity")
@ComponentScan("com.acgist.snail")
@SpringBootApplication
@EnableJpaRepositories(basePackages = "com.acgist.snail.repository")
@EnableTransactionManagement
public class AcgistSnailApplication {

	private static final Logger LOGGER = LoggerFactory.getLogger(AcgistSnailApplication.class);
	
	public static void main(String[] args) {
		LOGGER.info("蜗牛开始启动");
		buildWindow(args);
		buildSpring(args);
		LOGGER.info("蜗牛启动完成");
	}

	/**
	 * 创建窗口
	 */
	private static final void buildWindow(String[] args) {
		LOGGER.info("开始初始化窗口");
		Thread thread = new Thread(() -> {
			MainWindow.main(args);
		});
		thread.setName("蜗牛");
		thread.start();
	}
	
	/**
	 * 初始化Spring
	 */
	private static final void buildSpring(String[] args) {
		LOGGER.info("开始初始化Spring");
//		SpringApplication.run(AcgistSnailApplication.class, args);
		SpringApplicationBuilder builder = new SpringApplicationBuilder(AcgistSnailApplication.class);
		SpringApplication application = builder
	    	.headless(false)
	    	.web(WebApplicationType.NONE)
	    	.build();
		ApplicationContext context = application.run(args);
		SpringContextUtils.init(context);
	}
	
}