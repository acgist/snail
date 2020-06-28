/**
 * <h1>Sanil（蜗牛）</h1>
 * <p>基于Java、JavaFX开发的下载工具，支持下载协议：BT（BitTorrent、磁力链接、种子）、FTP、HTTP。</p>
 * <p>官网地址：https://gitee.com/acgist/snail</p>
 * 
 * TODO：稀有块、阻塞算法（Peer）
 * TODO：代码优化：添加测试、优化注释、日志优化、异常处理
 * 
 * @author acgist
 * @since 1.0.0
 */
open module com.acgist.snail {

	//================导出================//
	exports com.acgist.main;
	exports com.acgist.snail.gui;
	exports com.acgist.snail.gui.event;
	exports com.acgist.snail.net;
	exports com.acgist.snail.pojo;
	exports com.acgist.snail.pojo.bean;
	exports com.acgist.snail.pojo.message;
	exports com.acgist.snail.system;
	exports com.acgist.snail.system.format;
	exports com.acgist.snail.system.context;
	exports com.acgist.snail.protocol;
	exports com.acgist.snail.downloader;
	
	//================Java================//
	requires java.base; // 不能使用transitive修饰
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
	
	//================JDK================//
	requires transitive jdk.crypto.ec;
	requires transitive jdk.unsupported;
	
	//================JavaFX================//
	requires transitive javafx.fxml;
	requires transitive javafx.controls;
	requires transitive javafx.graphics;

	//================依赖================//
	requires transitive org.slf4j;
	requires transitive com.h2database;
	requires transitive ch.qos.logback.core;
	requires transitive ch.qos.logback.classic;

}