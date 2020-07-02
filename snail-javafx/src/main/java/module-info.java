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
open module com.acgist.snail.javafx {

	//================导出================//
	exports com.acgist.main;
	exports com.acgist.snail.gui.javafx;
	
	//================Java================//
	requires java.base;
	requires transitive java.desktop;
	
	//================JavaFX================//
	requires transitive javafx.fxml;
	requires transitive javafx.controls;
	requires transitive javafx.graphics;

	//================依赖================//
	requires transitive com.acgist.snail;

}