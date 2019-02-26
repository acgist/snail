/**
 * 模块化<br>
 * open：开放模块
 */
open module snail {
	
	/**
	 * 限定导出
	 **/
	exports com.acgist.main;
	
	/**
	 * opens：开放包（深层反射），使用open module不需要此配置。<br>
	 * opens com.acgist.main
	 */

	/**
	 * Java
	 */
	requires java.sql;
	requires java.base;
	requires java.desktop;
	requires java.net.http;
	
	/**
	 * jdeps 分析出来的依赖
	 */
//		java.sql
//		java.xml
//		java.base
//		java.naming
//		java.desktop
//		java.logging
//		java.scripting
//		java.management
//		jdk.unsupported
	
	/**
	 * JavaFX
	 */
	requires transitive javafx.fxml;
	requires transitive javafx.controls;
	requires transitive javafx.graphics;
	
	/**
	 * 其他依赖
	 */
	requires transitive h2;
	requires transitive slf4j.api;
	requires transitive com.fasterxml.jackson.core;
	requires transitive com.fasterxml.jackson.databind;
	requires transitive com.fasterxml.jackson.annotation;
}