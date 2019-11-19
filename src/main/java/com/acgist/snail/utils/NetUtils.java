package com.acgist.snail.utils;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.concurrent.atomic.AtomicInteger;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.acgist.snail.system.LambdaOptional;

/**
 * <p>网络工具</p>
 * <p>获取地址推荐使用方法：{@link InetAddress#getHostAddress()}、{@link InetSocketAddress#getHostString()}；不推荐使用：{@link InetSocketAddress#getHostName()};</p>
 * 
 * @author acgist
 * @since 1.0.0
 */
public final class NetUtils {

	private static final Logger LOGGER = LoggerFactory.getLogger(NetUtils.class);
	
	/**
	 * 本机名称
	 */
	private static final String LOCAL_HOST_NAME;
	/**
	 * 本机地址（多个物理地址获取其中一个）
	 */
	private static final String LOCAL_HOST_ADDRESS;
	/**
	 * 本机默认物理网卡（多个物理网卡获取其中一个）
	 */
	private static final NetworkInterface DEFAULT_NETWORK_INTERFACE;
	
	static {
		final LambdaOptional<String> localHostName = LambdaOptional.newInstance();
		final LambdaOptional<String> localHostAddress = LambdaOptional.newInstance();
		final LambdaOptional<NetworkInterface> defaultNetworkInterface = LambdaOptional.newInstance();
		try {
			final AtomicInteger index = new AtomicInteger(Integer.MAX_VALUE);
			NetworkInterface.networkInterfaces().forEach(networkInterface -> {
				final int nowIndex = networkInterface.getIndex();
				networkInterface.getInetAddresses().asIterator().forEachRemaining(inetAddress -> {
					if(
						index.get() > nowIndex && // 索引最小网卡
						inetAddress.isSiteLocalAddress() && // 本机地址
						!inetAddress.isAnyLocalAddress() && // 通配地址
						!inetAddress.isLoopbackAddress() && // 回环地址
						!inetAddress.isLinkLocalAddress() && // 连接地址：虚拟网卡
						!inetAddress.isMulticastAddress() // 广播地址
					) {
						index.set(nowIndex);
//						localHostName.set(inetAddress.getHostName()); // 速度太慢
						localHostAddress.set(inetAddress.getHostAddress());
						defaultNetworkInterface.set(networkInterface);
					}
				});
			});
		} catch (SocketException e) {
			LOGGER.error("初始化本地网络信息异常", e);
		}
		try {
			localHostName.set(InetAddress.getLocalHost().getHostName());
		} catch (UnknownHostException e) {
			LOGGER.error("初始化本地名称异常", e);
		}
		LOCAL_HOST_NAME = localHostName.get();
		LOCAL_HOST_ADDRESS = localHostAddress.get();
		DEFAULT_NETWORK_INTERFACE = defaultNetworkInterface.get();
		LOGGER.info("本地名称：{}", LOCAL_HOST_NAME);
		LOGGER.info("本地地址：{}", LOCAL_HOST_ADDRESS);
		LOGGER.info("本地默认物理网卡：{}", DEFAULT_NETWORK_INTERFACE);
	}
	
	/**
	 * 最大端口号
	 */
	public static final int MAX_PORT = 2 << 15;
	/**
	 * 本机IP地址
	 */
	public static final String LOCAL_IP = "127.0.0.1";
	/**
	 * 本机HOST
	 */
	public static final String LOCAL_HOST = "localhost";
	/**
	 * IP地址正则表达式
	 */
	private static final String IP_REGEX = "(\\d{0,3}\\.){3}\\d{0,3}";
	/**
	 * <p>A类私用地址</p>
	 * <p>A类范围：0.0.0.0-127.255.255.255</p>
	 * <p>默认子网掩码：255.0.0.0</p>
	 */
	private static final long A_NATIVE_IP_BEGIN = encodeIpToLong("10.0.0.0");
	private static final long A_NATIVE_IP_END = encodeIpToLong("10.255.255.255");
	/**
	 * <p>B类私用地址</p>
	 * <p>B类范围：128.0.0.0-191.255.255.255</p>
	 * <p>默认子网掩码：255.255.0.0</p>
	 */
	private static final long B_NATIVE_IP_BEGIN = encodeIpToLong("172.16.0.0");
	private static final long B_NATIVE_IP_END = encodeIpToLong("172.31.255.255");
	/**
	 * <p>C类私用地址</p>
	 * <p>C类范围：192.0.0.0-223.255.255.255</p>
	 * <p>默认子网掩码：255.255.255.0</p>
	 */
	private static final long C_NATIVE_IP_BEGIN = encodeIpToLong("192.168.0.0");
	private static final long C_NATIVE_IP_END = encodeIpToLong("192.168.255.255");
	/**
	 * 本地回环地址
	 */
	private static final long L_NATIVE_IP_BEGIN = encodeIpToLong("127.0.0.0");
	private static final long L_NATIVE_IP_END = encodeIpToLong("127.255.255.255");
	
	/**
	 * <p>端口编码</p>
	 * <p>int端口转换为short</p>
	 */
	public static final short encodePort(int port) {
		return (short) port;
	}

	/**
	 * <p>端口解码</p>
	 * <p>short端口转换为int</p>
	 */
	public static final int decodePort(short port) {
		return Short.toUnsignedInt(port);
	}
	
	/**
	 * <p>IP地址编码</p>
	 * <p>IP地址转换为int</p>
	 */
	public static final int encodeIpToInt(String ip) {
		return (int) encodeIpToLong(ip);
	}
	
	/**
	 * <p>IP地址编码</p>
	 * <p>IP地址转换为long</p>
	 */
	public static final long encodeIpToLong(String ip) {
		long result = 0, value;
		final String[] array = ip.split("\\.");
		for (int index = 3; index >= 0; index--) {
			value = Long.parseLong(array[3 - index]);
			result |= value << (index * 8);
		}
		return result;
	}

	/**
	 * <p>IP地址解码</p>
	 * <p>int转换为IP地址</p>
	 */
	public static final String decodeIntToIp(int value) {
		return decodeLongToIp(Integer.toUnsignedLong(value));
	}
	
	/**
	 * <p>IP地址解码</p>
	 * <p>long转换为IP地址</p>
	 */
	public static final String decodeLongToIp(long value) {
		return
			((value >> 24) & 0xFF) + "." +
			((value >> 16) & 0xFF) + "." +
			((value >> 8) & 0xFF) + "." +
			(value & 0xFF);
	}
	
	/**
	 * <p>IPv6地址编码</p>
	 */
	public static final byte[] encodeIPv6(String ip) {
		try {
			return InetAddress.getByName(ip).getAddress();
		} catch (UnknownHostException e) {
			LOGGER.error("IPv6地址转换异常：{}", ip, e);
		}
		return null;
	}
	
	/**
	 * <p>IPv6地址解码</p>
	 */
	public static final String decodeIPv6(byte[] value) {
		try {
			return InetAddress.getByAddress(value).getHostAddress();
		} catch (UnknownHostException e) {
			LOGGER.error("IPv6地址转换异常", e);
		}
		return null;
	}
	
	/**
	 * 获取本机名称
	 */
	public static final String localHostName() {
		return LOCAL_HOST_NAME;
	}

	/**
	 * 获取本机地址
	 */
	public static final String localHostAddress() {
		return LOCAL_HOST_ADDRESS;
	}
	
	/**
	 * 获取本机默认物理网卡
	 */
	public static final NetworkInterface defaultNetworkInterface() {
		return DEFAULT_NETWORK_INTERFACE;
	}
	
	/**
	 * 验证IP地址
	 */
	public static final boolean isIp(String host) {
		return StringUtils.regex(host, IP_REGEX, true);
	}

	/**
	 * 判断是否是本地IP地址
	 */
	public static final boolean isLocalIp(String ip) {
		final long value = encodeIpToLong(ip);
		return
			(A_NATIVE_IP_BEGIN <= value && value <= A_NATIVE_IP_END) ||
			(B_NATIVE_IP_BEGIN <= value && value <= B_NATIVE_IP_END) ||
			(C_NATIVE_IP_BEGIN <= value && value <= C_NATIVE_IP_END) ||
			(L_NATIVE_IP_BEGIN <= value && value <= L_NATIVE_IP_END);
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
	
}
