package com.acgist.snail.net.bt.tracker;

import java.net.http.HttpResponse.BodyHandlers;

import com.acgist.snail.net.http.HttpUtils;

/**
 * tracker http 客户端
 */
public class TrackerHttpClient {
	
	public static void main(String[] args) {
		TrackerHttpClient c = new TrackerHttpClient();
		c.decode("http://btfile.sdo.com:6961/announce", null);
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
	 * 构建请求URL
	 * info_hash：与种子文件中info关键字对应的值，通过Sha1算法计算其hash值，该hash值就是info_hash参数对应的值，该hash值的长度固定为20字节
	 * peer_id：每个客户端在下载文件前以随机的方式生成的20字节的标识符，用于标识自己，它的长度也是固定不变的
	 * port：监听端口号，用于接收其他peer的连接请求
	 * uploaded：当前总的上传量，以字节为单位
	 * downloaded：当前总的下载量，以字节为单位
	 * left：还剩余多少字节需要下载，以字节为单位
	 * compact：该参数的值一般为1
	 * event：它的值为started、completed、stopped其中之一。客户端第一次与Tracker进行通信时，该值为started；下载完成时，该值为completed；客户端即将关闭时，该值为stopped
	 * ip：可选，将客户端的IP地址告知给Tracker，Tracker可以通过分析客户端发给Tracker的IP数据包来获取客户端的IP地址，因此该参数是可选的，一般不用指明客户端的IP
	 * numwant：可选，希望Tracker返回多少个peer的IP地址和端口号。如果该参数缺省，则默认返回50个peer的IP地址和端口号
	 * key：可选，它的值为一个随机数，用于进一步标识客户端。因为已经由peer_id来标识客户端，因此该参数一般不使用
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
