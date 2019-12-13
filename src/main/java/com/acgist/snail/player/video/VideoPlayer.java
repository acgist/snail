package com.acgist.snail.player.video;

/**
 * <p>视频播放器</p>
 * <p>视频比特率：{@code 1kByte/s=8kbit/s}</p>
 * 
 * @author acgist
 * @since 1.3.0
 */
public final class VideoPlayer {

////	exchange.getResponseHeaders().add("Accept-Ranges", "bytes"); // 关闭就是直播
//	exchange.getResponseHeaders().add("Content-Type", "video/mp4");
//	File file = new File("E:/gitee/snail/download/[Nekomoe kissaten][SUPER SHIRO][09][1080p][CHS].mp4/[Nekomoe kissaten][SUPER SHIRO][09][1080p][CHS].mp4");
//	final var input = new FileInputStream(file);
//	exchange.getResponseHeaders().add("Content-Length", input.available() + "");
//	String Range = exchange.getRequestHeaders().getFirst("Range");
//	if(Range != null) {
//		exchange.getResponseHeaders().add("Content-Range", "102400-" + input.available() + "/" + input.available());
//	}
//	exchange.sendResponseHeaders(200, 0);
//	int index;
//	byte[] bytes = new byte[128 * 1024]; // 计算每秒数据
//	while((index = input.read(bytes)) != -1) {
//		exchange.getResponseBody().write(bytes);
//	}
	
}
