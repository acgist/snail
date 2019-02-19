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

import com.acgist.killer.context.SpringContextUtils;
import com.acgist.killer.window.main.MainWindow;

/**
 * 启动类
 */
@EntityScan("com.acgist.killer.pojo.entity")
@ComponentScan("com.acgist.killer")
@SpringBootApplication
@EnableJpaRepositories(basePackages = "com.acgist.killer.repository")
@EnableTransactionManagement
public class AcgistKillerApplication {

	private static final Logger LOGGER = LoggerFactory.getLogger(AcgistKillerApplication.class);
	
	public static void main(String[] args) {
		LOGGER.info("ACGIST-KILLER开始启动");
		buildWindow(args);
		buildSpring(args);
		LOGGER.info("ACGIST-KILLER启动完成");
	}

	/**
	 * 创建窗口
	 */
	private static final void buildWindow(String[] args) {
		LOGGER.info("ACGIST-KILLER启动窗口");
		Thread thread = new Thread(() -> {
			MainWindow.main(args);
		});
		thread.setName("ACGIST-KILLER Window");
		thread.start();
	}
	
	/**
	 * 初始化Spring
	 */
	private static final void buildSpring(String[] args) {
		LOGGER.info("ACGIST-KILLER启动Spring");
//		SpringApplication.run(AcgistKillerApplication.class, args);
		SpringApplicationBuilder builder = new SpringApplicationBuilder(AcgistKillerApplication.class);
		SpringApplication application = builder
	    	.headless(false)
	    	.web(WebApplicationType.NONE)
	    	.build();
		ApplicationContext context = application.run(args);
		SpringContextUtils.init(context);
	}
	
}