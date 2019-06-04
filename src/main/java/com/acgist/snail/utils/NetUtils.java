package com.acgist.snail.utils;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.StandardProtocolFamily;
import java.net.UnknownHostException;
import java.nio.channels.DatagramChannel;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * <p>网络工具</p>
 * 
 * @author acgist
 * @since 1.0.0
 */
public class NetUtils {

	private static final Logger LOGGER = LoggerFactory.getLogger(NetUtils.class);
	
	/**
	 * 最大端口号
	 */
	public static final int MAX_PORT = 2 << 15;
	
	/**
	 * 本机IP
	 */
	public static final String LOCAL_IP = "127.0.0.1";
	/**
	 * 本机HOST
	 */
	public static final String LOCAL_HOST = "localhost";
	
	// A类私用地址
	private static final long A_IP_BEGIN = encodeIpToLong("10.0.0.0");
	private static final long A_IP_END = encodeIpToLong("10.255.255.255");
	// B类私用地址
	private static final long B_IP_BEGIN = encodeIpToLong("172.16.0.0");
	private static final long B_IP_END = encodeIpToLong("172.31.255.255");
	// C类私用地址
	private static final long C_IP_BEGIN = encodeIpToLong("192.168.0.0");
	private static final long C_IP_END = encodeIpToLong("192.168.255.255");
	// 系统环回地址
	private static final long L_IP_BEGIN = encodeIpToLong("127.0.0.0");
	private static final long L_IP_END = encodeIpToLong("127.255.255.255");
	
	/**
	 * IP正则表达式
	 */
	private static final String IP_REGEX = "(\\d{0,3}\\.){3}\\d{0,3}";
	
	/**
	 * 验证IP地址
	 */
	public static final boolean verifyIp(String host) {
		return StringUtils.regex(host, IP_REGEX, true);
	}
	
	/**
	 * <p>端口编码</p>
	 * <p>int端口转换为short。</p>
	 */
	public static final short encodePort(int port) {
		return (short) port;
	}

	/**
	 * <p>端口解码</p>
	 * <p>short端口转换为int。</p>
	 */
	public static final int decodePort(short port) {
		return Short.toUnsignedInt(port);
	}
	
	/**
	 * <p>IP编码</p>
	 * <p>IP地址转换为int。</p>
	 */
	public static final int encodeIpToInt(String ip) {
		return (int) encodeIpToLong(ip);
	}
	
	/**
	 * <p>IP编码</p>
	 * <p>IP地址转换为long。</p>
	 */
	public static final long encodeIpToLong(String ip) {
		long result = 0, tmp;
		final String[] array = ip.split("\\.");
		for (int index = 3; index >= 0; index--) {
			tmp = Long.parseLong(array[3 - index]);
			result |= tmp << (index * 8);
		}
		return result;
	}

	/**
	 * <p>IP解码</p>
	 * <p>int转换为IP地址。</p>
	 */
	public static final String decodeIntToIp(int value) {
		return decodeLongToIp(Integer.toUnsignedLong(value));
	}
	
	/**
	 * <p>IP解码</p>
	 * <p>long转换为IP地址。</p>
	 */
	public static final String decodeLongToIp(long value) {
		return ((value >> 24) & 0xFF) + "." + ((value >> 16) & 0xFF) + "." + ((value >> 8) & 0xFF) + "." + (value & 0xFF);
	}
	
	/**
	 * 获取本机名称
	 * TODO：初始化一次
	 */
	public static final String inetHostName() {
		try {
			final InetAddress address = InetAddress.getLocalHost();
			return address.getHostName();
		} catch (UnknownHostException e) {
			LOGGER.error("获取本机名称异常", e);
		}
		return null;
	}

	/**
	 * 获取本机地址
	 * TODO：初始化一次
	 */
	public static final String inetHostAddress() {
		try {
			final InetAddress address = InetAddress.getLocalHost();
			return address.getHostAddress();
		} catch (UnknownHostException e) {
			LOGGER.error("获取本机地址异常", e);
		}
		return null;
	}
	
	/**
	 * 获取网络接口
	 * TODO：初始化一次
	 */
	public static final NetworkInterface defaultNetworkInterface() {
		final String hostAddress = inetHostAddress();
		Optional<NetworkInterface> optional = null;
		try {
			optional = NetworkInterface.networkInterfaces().filter(interfaces -> {
				return interfaces.inetAddresses().anyMatch(addresses -> {
					return addresses.getHostAddress().equals(hostAddress);
				});
			}).findFirst();
		} catch (SocketException e) {
			LOGGER.error("获取网络接口异常", e);
		}
		if(optional == null || optional.isEmpty()) {
			return null;
		}
		return optional.get();
	}

	/**
	 * 判断是否是本地IP地址。
	 */
	public static final boolean isLocalIp(String ip) {
		final long value = encodeIpToLong(ip);
		return
			(A_IP_BEGIN < value && value < A_IP_END) ||
			(B_IP_BEGIN < value && value < B_IP_END) ||
			(C_IP_BEGIN < value && value < C_IP_END) ||
			(L_IP_BEGIN < value && value < L_IP_END);
	}
	
	/**
	 * 本机IP
	 */
	public static final boolean isLocalhost(String host) {
		return LOCAL_IP.equals(host);
	}
	
	/**
	 * 创建本地socket地址
	 * 
	 * @param port 端口
	 */
	public static final InetSocketAddress buildSocketAddress(final int port) {
		return buildSocketAddress(null, port);
	}
	
	/**
	 * 创建socket地址
	 * 
	 * @param host 地址
	 * @param port 端口
	 */
	public static final InetSocketAddress buildSocketAddress(final String host, final int port) {
		if(StringUtils.isEmpty(host)) {
			return new InetSocketAddress(port);
		} else {
			return new InetSocketAddress(host, port);
		}
	}
	
	/**
	 * <p>创建UDP通道</p>
	 * <p>host=本机、port=随机。</p>
	 */
	public static final DatagramChannel buildUdpChannel() {
		return buildUdpChannel(-1);
	}
	
	/**
	 * <p>创建UDP通道</p>
	 * <p>host=本机</p>
	 */
	public static final DatagramChannel buildUdpChannel(final int port) {
		return buildUdpChannel(null, port);
	}
	
	/**
	 * <p>创建UDP通道</p>
	 * 
	 * @param port -1=不绑定端口，随机选择。
	 */
	public static final DatagramChannel buildUdpChannel(final String host, final int port) {
		DatagramChannel channel = null;
		try {
			channel = DatagramChannel.open(StandardProtocolFamily.INET); // TPv4
			channel.configureBlocking(false); // 不阻塞
//			channel.connect(NetUtils.buildSocketAddress(host, port)); // 连接后使用：read、write
			if(port >= 0) {
				channel.bind(NetUtils.buildSocketAddress(host, port)); // 监听端口：UDP服务端和客户端使用同一个端口
			}
		} catch (IOException e) {
			IoUtils.close(channel);
			channel = null;
			LOGGER.error("打开UDP通道异常", e);
		}
		return channel;
	}

}
