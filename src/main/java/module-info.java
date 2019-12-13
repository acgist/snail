/**
 * <h1>Sanil（蜗牛）</h1>
 * <p>基于Java、JavaFX开发的下载工具，支持下载协议：BT（BitTorrent）、FTP、HTTP。</p>
 * <p>开放整个模块（反射使用）</p>
 * 
 * TODO：稀有块、阻塞算法（Peer）、图标选中切换颜色
 * TODO：每天优化一个包的代码
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
	exports com.acgist.snail.pojo.message;
	exports com.acgist.snail.protocol;
	exports com.acgist.snail.downloader;
	exports com.acgist.snail.system;
	exports com.acgist.snail.system.context;
	
	//================Java================//
	requires java.base; // java.base不能使用transitive修饰
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
	requires transitive jdk.httpserver;
	requires transitive jdk.unsupported;
	
	//================JavaFX================//
	requires transitive javafx.fxml;
	requires transitive javafx.controls;
	requires transitive javafx.graphics;

	//================其他依赖================//
	requires transitive org.slf4j;
	requires transitive com.h2database;
	requires transitive ch.qos.logback.core;
	requires transitive ch.qos.logback.classic;

}