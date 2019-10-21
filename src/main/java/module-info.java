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
 * <p><i>阿里代码规范检测：https://p3c.alibaba.com/plugin/eclipse/update</i></p>
 * 
 * <h4>代码顺序</h4>
 * <p>LOGGER、单例（INSTANCE）、枚举、常量、静态变量、静态代码块、成员变量</p>
 * <p>构造方法、单例方法（getInstance、newInstance）、静态方法、类方法（抽象方法、public、protected、private）</p>
 * <p>Getter、Setter、重写Object方法（hashCode、equals、toString）</p>
 * <p>内部类</p>
 * 
 * <h2>质量检测</h2>
 * <p>Sonar</p>
 * <pre>
 * mvn sonar:sonar "-Dsonar.projectKey=snail" "-Dsonar.host.url=http://localhost:9000" "-Dsonar.login=token"
 * </pre>
 * 
 * @author acgist
 * @since 1.0.0
 */
open module com.acgist.snail {

	/*
	 * 限定导出
	 */
	exports com.acgist.main;
//	exports com.acgist.snail.gui;
//	exports com.acgist.snail.net;
//	exports com.acgist.snail.protocol;
//	exports com.acgist.snail.downloader;

	/*
	 * opens：开放包（反射时需要，使用open module不需要此配置）
	 * opens com.acgist.main;
	 */

	/*
	 * Java
	 */
	requires java.sql;
	requires java.base;
	requires java.desktop;
	requires java.net.http;
	
	/*
	 * 依赖：jdeps --list-deps *.jar
	 * java.xml
	 * java.naming
	 * java.logging
	 * java.compiler
	 * java.scripting
	 * java.instrument
	 * java.management
	 * java.transaction.xa
	 * jdk.crypto.ec
	 * jdk.unsupported
	 */

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