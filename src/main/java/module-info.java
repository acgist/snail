/**
 * <h1>Sanil（蜗牛）下载工具</h1>
 * <p>Snail（蜗牛）是一款下载软件，支持下载协议：BT（BitTorrent）、FTP、HTTP。</p>
 * 
 * <h2>规范</h2>
 * <h4>注释、注解</h4>
 * <p>所有的类、抽象方法必须使用javadoc注解。</p>
 * <p>所有的类变量、静态变量需要使用javadoc注解。</p>
 * <p>允许使用同行注释。</p>
 * 
 * <h2>规范检测</h2>
 * <p>P3C：https://github.com/alibaba/p3c</p>
 * <p>Eclipse：https://p3c.alibaba.com/plugin/eclipse/update</p>
 * 
 * <h2>质量检测</h2>
 * <p>Sonar</p>
 * <pre>
 * mvn sonar:sonar "-Dsonar.projectKey=snail" "-Dsonar.host.url=http://localhost:9000" "-Dsonar.login=token"
 * </pre>
 * 
 * TODO：稀有块、阻塞算法（Peer）
 * 
 * @author acgist
 * @since 1.0.0
 */
open module com.acgist.snail {

	/*
	 * 限定导出
	 */
	exports com.acgist.main;
	exports com.acgist.snail.gui;
	exports com.acgist.snail.gui.event;
	exports com.acgist.snail.net;
	exports com.acgist.snail.pojo;
	exports com.acgist.snail.pojo.message;
	exports com.acgist.snail.protocol;
	exports com.acgist.snail.downloader;

	/*
	 * opens：开放包
	 * opens com.acgist.main;
	 * 反射时需要：使用open module不需要此配置
	 */

	/*
	 * Java
	 */
	requires java.base;
	
	/*
	 * Java
	 */
	requires transitive java.sql;
	requires transitive java.xml;
	requires transitive java.naming;
	requires transitive java.logging;
	requires transitive java.desktop;
	requires transitive java.net.http;
	requires transitive java.compiler;
	requires transitive java.scripting;
	requires transitive java.instrument;
	requires transitive java.management;
	requires transitive java.transaction.xa;
	
	/*
	 * JDK
	 */
	requires transitive jdk.crypto.ec;
	requires transitive jdk.unsupported;

	/*
	 * JavaFX
	 */
	requires transitive javafx.fxml;
	requires transitive javafx.controls;
	requires transitive javafx.graphics;

	/*
	 * 其他：数据库、日志
	 */
	requires transitive org.slf4j;
	requires transitive com.h2database;
	requires transitive ch.qos.logback.core;
	requires transitive ch.qos.logback.classic;

}