package com.acgist.snail.net.bt.tracker;

import java.net.http.HttpResponse.BodyHandlers;

import com.acgist.snail.net.http.HttpUtils;

/**
 * tracker http 客户端
 */
public class TrackerHttpClient {
	
	public static void main(String[] args) {
		TrackerHttpClient c = new TrackerHttpClient();
		c.decode("http://t.acg.rip:6699/announce", null);
	}
	
	/**
	 * 解析HTTP tracker
	 */
	public void decode(String trackerUrl, String hash) {
		String requestUrl = buildUrl(trackerUrl);
		var client = HttpUtils.newClient();
		System.out.println(requestUrl);
		var request = HttpUtils.newRequest(requestUrl)
			.GET()
			.build();
		request.headers().map().forEach((key, value) -> {
			System.out.println(key + "=" + value);
		});
		var response = HttpUtils.request(client, request, BodyHandlers.ofString());
		if(response != null) {
			System.out.println(response.body());
		}
	}
	
	/**
	 * 构建请求URL<br>
	 * info_hash：种子hash，长度固定为20字节<br>
	 * peer_id：客户端在下载文件前以随机的方式生成的20字节的标识符，用于标识自己<br>
	 * port：监听端口号，用于接收其他peer的连接请求<br>
	 * uploaded：当前总的上传量，以字节为单位<br>
	 * downloaded：当前总的下载量，以字节为单位<br>
	 * left：剩余需要下载量，以字节为单位<br>
	 * compact：一般默认：1<br>
	 * event：选择以下值：started、completed、stopped。第一次交互：started；下载完成：completed；客户端即将关闭：stopped<br>
	 * ip：可选，客户端IP<br>
	 * numwant：可选，希望返回的peer数，默认：50<br>
	 * key：可选，一个随机数，用于进一步标识客户端<br>
	 * trackerid：可选，一般不使用
	 */
	private String buildUrl(String trackerUrl) {
		StringBuilder builder = new StringBuilder(trackerUrl);
		builder.append("?")
		.append("info_hash").append("=").append("%6c%ac%97%70%e6%1c%bf%64%c7%08%2e%07%47%98%bb%75%eb%e6%0d%ab").append("&")
		.append("peer_id").append("=").append("82309348090ecbec8bf509b83b30b78a8d1f6454".substring(0, 20)).append("&")
		.append("port").append("=").append("28888").append("&")
		.append("uploaded").append("=").append("0").append("&")
		.append("downloaded").append("=").append("0").append("&")
		.append("left").append("=").append("10000").append("&")
		.append("compact").append("=").append("1").append("&")
		.append("event").append("=").append("started");
		return builder.toString();
	}

}
