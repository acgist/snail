/**
 * <h1>Sanil（蜗牛）</h1>
 * <p>通过系统消息管理Snail（蜗牛）</p>
 * <p>官网地址：https://gitee.com/acgist/snail</p>
 * 
 * @author acgist
 * @since 1.4.0
 */
open module com.acgist.snail.extend {

	//================导出================//
	exports com.acgist.main;
	
	//================Java================//
	requires java.base;
	
	//================依赖================//
	requires transitive com.acgist.snail;

}