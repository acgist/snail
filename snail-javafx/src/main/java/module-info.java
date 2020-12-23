/**
 * <h1>Sanil（蜗牛）</h1>
 * <p>通过JavaFX开发的图形界面管理Snail（蜗牛）</p>
 * <p>官网地址：https://gitee.com/acgist/snail</p>
 * 
 * @author acgist
 */
open module com.acgist.snail.javafx {

	//================导出================//
	exports com.acgist.main;
	exports com.acgist.snail.gui.javafx;
	
	//================Java================//
	requires java.base;
	// GUI依赖：AWT、Swing
	requires transitive java.desktop;
	// JavaFX依赖
	requires transitive java.scripting;
	
	//================JDK================//
	// JavaFX依赖
	requires transitive jdk.unsupported;
	
	//================JavaFX================//
	requires transitive javafx.fxml;
	requires transitive javafx.controls;
	requires transitive javafx.graphics;

	//================依赖================//
	requires transitive com.acgist.snail;

}