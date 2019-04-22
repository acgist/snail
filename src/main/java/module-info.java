/**
 * Sanil下载工具，支持下载协议：BT、FTP、HTTP、ED2K。<br>
 * open：开放模块<br>
 */
open module com.acgist.snail {
	
	/**
	 * 限定导出
	 **/
	exports com.acgist.main;
	
	/**
	 * opens：开放包（反射时需要，使用open module不需要此配置）<br>
	 * opens com.acgist.main;
	 */

	/**
	 * Java
	 */
	requires java.sql;
	requires java.xml;
	requires java.base;
	requires java.desktop;
	requires java.net.http;
	
	/*
	 * jdeps分析出来的依赖：
	 */
//	java.sql
//	java.xml
//	java.base
//	java.naming
//	java.desktop
//	java.logging
//	java.scripting
//	java.management
//	jdk.unsupported
	
	/**
	 * JavaFX
	 */
	requires transitive javafx.fxml;
	requires transitive javafx.controls;
	requires transitive javafx.graphics;
	
	/**
	 * 其他依赖
	 */
	requires transitive org.slf4j;
	requires transitive com.h2database;
	requires transitive ch.qos.logback.core;
	requires transitive ch.qos.logback.classic;
	requires transitive com.fasterxml.jackson.core;
	requires transitive com.fasterxml.jackson.databind;
	requires transitive com.fasterxml.jackson.annotation;
	
}