package com.acgist.snail.utils;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * <p>网络工具</p>
 * <p>获取地址方法：{@link InetAddress#getHostAddress()}、{@link InetSocketAddress#getHostString()}、<del>{@link InetSocketAddress#getHostName()}</del></p>
 * 
 * @author acgist
 */
public final class NetUtils {

	private static final Logger LOGGER = LoggerFactory.getLogger(NetUtils.class);
	
	/**
	 * <p>本机名称</p>
	 */
	private static final String LOCAL_HOST_NAME;
	/**
	 * <p>本机地址</p>
	 */
	private static final String LOCAL_HOST_ADDRESS;
	/**
	 * <p>子网掩码</p>
	 */
	private static final int LOCAL_HOST_MASK;
	/**
	 * <p>本机默认物理网卡</p>
	 */
	private static final NetworkInterface DEFAULT_NETWORK_INTERFACE;
	/**
	 * <p>最大端口号：{@value}</p>
	 */
	public static final int MAX_PORT = 2 << 15;
	/**
	 * <p>本机IP地址：{@value}</p>
	 */
	public static final String LOCAL_IP = "127.0.0.1";
	/**
	 * <p>本机HOST地址：{@value}</p>
	 */
	public static final String LOCAL_HOST = "localhost";
	/**
	 * <p>IPv4地址正则表达式：{@value}</p>
	 */
	private static final String IP_REGEX = "(\\d{0,3}\\.){3}\\d{0,3}";
	/**
	 * <p>A类私用地址</p>
	 * <p>A类地址范围：0.0.0.0-127.255.255.255</p>
	 * <p>默认子网掩码：255.0.0.0</p>
	 */
	private static final long NATIVE_A_IP_BEGIN = ipToLong("10.0.0.0");
	/**
	 * @see #NATIVE_A_IP_BEGIN
	 */
	private static final long NATIVE_A_IP_END = ipToLong("10.255.255.255");
	/**
	 * <p>B类私用地址</p>
	 * <p>B类地址范围：128.0.0.0-191.255.255.255</p>
	 * <p>默认子网掩码：255.255.0.0</p>
	 */
	private static final long NATIVE_B_IP_BEGIN = ipToLong("172.16.0.0");
	/**
	 * @see #NATIVE_B_IP_BEGIN
	 */
	private static final long NATIVE_B_IP_END = ipToLong("172.31.255.255");
	/**
	 * <p>C类私用地址</p>
	 * <p>C类地址范围：192.0.0.0-223.255.255.255</p>
	 * <p>默认子网掩码：255.255.255.0</p>
	 */
	private static final long NATIVE_C_IP_BEGIN = ipToLong("192.168.0.0");
	/**
	 * @see #NATIVE_C_IP_BEGIN
	 */
	private static final long NATIVE_C_IP_END = ipToLong("192.168.255.255");
	/**
	 * <p>本地回环地址</p>
	 */
	private static final long NATIVE_L_IP_BEGIN = ipToLong("127.0.0.0");
	/**
	 * @see #NATIVE_L_IP_BEGIN
	 */
	private static final long NATIVE_L_IP_END = ipToLong("127.255.255.255");
	
	static {
		final ModifyOptional<String> localHostName = ModifyOptional.newInstance();
		final ModifyOptional<String> localHostAddress = ModifyOptional.newInstance();
		final ModifyOptional<Integer> localHostMask = ModifyOptional.newInstance();
		final ModifyOptional<NetworkInterface> defaultNetworkInterface = ModifyOptional.newInstance();
		try {
			final AtomicInteger index = new AtomicInteger(Integer.MAX_VALUE);
			// 处理多个物理网卡和虚拟网卡
			NetworkInterface.networkInterfaces().forEach(networkInterface -> {
				final int nowIndex = networkInterface.getIndex();
				networkInterface.getInterfaceAddresses().forEach(interfaceAddress -> {
					final var address = interfaceAddress.getAddress(); // 地址
//					final var broadcast = interfaceAddress.getBroadcast(); // 广播地址
					if(
						index.get() > nowIndex && // 索引最小网卡
						address.isSiteLocalAddress() && // 本机地址
						!address.isAnyLocalAddress() && // 通配地址
						!address.isLoopbackAddress() && // 回环地址
						!address.isLinkLocalAddress() && // 连接地址：虚拟网卡
						!address.isMulticastAddress() // 广播地址
					) {
						index.set(nowIndex);
						localHostName.set(buildLocalHostName());
//						localHostName.set(address.getHostName()); // 速度太慢
						localHostAddress.set(address.getHostAddress());
						defaultNetworkInterface.set(networkInterface);
						final var length = interfaceAddress.getNetworkPrefixLength(); // mask
						localHostMask.set(-1 << (32 - length));
					}
				});
			});
		} catch (SocketException e) {
			LOGGER.error("初始化本机网络信息异常", e);
		}
		LOCAL_HOST_NAME = localHostName.get();
		LOCAL_HOST_ADDRESS = localHostAddress.get();
		LOCAL_HOST_MASK = localHostMask.get();
		DEFAULT_NETWORK_INTERFACE = defaultNetworkInterface.get();
		LOGGER.info("本机名称：{}", LOCAL_HOST_NAME);
		LOGGER.info("本机地址：{}", LOCAL_HOST_ADDRESS);
		LOGGER.info("子网掩码：{}", LOCAL_HOST_MASK);
		LOGGER.info("本机默认物理网卡：{}", DEFAULT_NETWORK_INTERFACE);
	}
	
	/**
	 * <p>工具类禁止实例化</p>
	 */
	private NetUtils() {
	}
	
	/**
	 * <p>端口编码</p>
	 * 
	 * @param port 端口（int）
	 * 
	 * @return 端口（short）
	 */
	public static final short portToShort(int port) {
		return (short) port;
	}

	/**
	 * <p>端口解码</p>
	 * 
	 * @param port 端口（short）
	 * 
	 * @return 端口（int）
	 */
	public static final int portToInt(short port) {
		return Short.toUnsignedInt(port);
	}
	
	/**
	 * <p>IP地址编码</p>
	 * 
	 * @param ip IP地址（字符串）
	 * 
	 * @return IP地址（int）
	 */
	public static final int ipToInt(String ip) {
		return (int) ipToLong(ip);
	}
	
	/**
	 * <p>IP地址编码</p>
	 * 
	 * @param ip IP地址（字符串）
	 * 
	 * @return IP地址（long）
	 */
	public static final long ipToLong(String ip) {
		Objects.requireNonNull(ip, "IP地址不能为空");
		final String[] array = ip.split("\\.");
		if(array.length != 4) {
			throw new IllegalArgumentException("IP格式错误：" + ip);
		}
		long value;
		long result = 0;
		for (int index = 3; index >= 0; index--) {
			value = Long.parseLong(array[3 - index]);
			result |= value << (index * 8);
		}
		return result;
	}

	/**
	 * <p>IP地址解码</p>
	 * 
	 * @param value IP地址（int）
	 * 
	 * @return IP地址（字符串）
	 */
	public static final String intToIP(int value) {
		return longToIP(Integer.toUnsignedLong(value));
	}
	
	/**
	 * <p>IP地址解码</p>
	 * 
	 * @param value IP地址（long）
	 * 
	 * @return IP地址（字符串）
	 */
	public static final String longToIP(long value) {
		return
			((value >> 24) & 0xFF) + "." +
			((value >> 16) & 0xFF) + "." +
			((value >> 8) & 0xFF) + "." +
			(value & 0xFF);
	}
	
	/**
	 * <p>IPv6地址编码</p>
	 * 
	 * @param ip IP地址（字符串）
	 * 
	 * @return IP地址（字节数组）
	 */
	public static final byte[] bytesToIP(String ip) {
		try {
			return InetAddress.getByName(ip).getAddress();
		} catch (UnknownHostException e) {
			LOGGER.error("IPv6地址编码异常：{}", ip, e);
		}
		return null;
	}
	
	/**
	 * <p>IPv6地址解码</p>
	 * 
	 * @param value IP地址（字节数组）
	 * 
	 * @return IP地址（字符串）
	 */
	public static final String ipToBytes(byte[] value) {
		try {
			return InetAddress.getByAddress(value).getHostAddress();
		} catch (UnknownHostException e) {
			LOGGER.error("IPv6地址解码异常", e);
		}
		return null;
	}
	
	/**
	 * @return 本机名称
	 */
	public static final String localHostName() {
		return LOCAL_HOST_NAME;
	}

	/**
	 * @return 本机地址
	 */
	public static final String localHostAddress() {
		return LOCAL_HOST_ADDRESS;
	}
	
	/**
	 * @return 本机默认物理网卡
	 */
	public static final NetworkInterface defaultNetworkInterface() {
		return DEFAULT_NETWORK_INTERFACE;
	}

	/**
	 * <p>判断是否是同一个网关</p>
	 * 
	 * @param host 地址
	 * 
	 * @return 是否是同一个网关
	 */
	public static final boolean gateway(String host) {
		if(ipAddress(host)) {
			final int value = ipToInt(host);
			final int localHostValue = ipToInt(LOCAL_HOST_ADDRESS);
			return (value & LOCAL_HOST_MASK) == (localHostValue & LOCAL_HOST_MASK);
		}
		return false;
	}
	
	/**
	 * <p>获取本机名称</p>
	 * 
	 * @return 本机名称
	 */
	private static final String buildLocalHostName() {
		try {
			return InetAddress.getLocalHost().getHostName();
		} catch (UnknownHostException e) {
			LOGGER.error("获取本机名称异常", e);
		}
		return null;
	}
	
	/**
	 * <p>是否是IP地址</p>
	 * 
	 * @param host IP地址
	 * 
	 * @return true-是；false-不是；
	 * 
	 * TODO：IPv6
	 */
	public static final boolean ipAddress(String host) {
		return StringUtils.regex(host, IP_REGEX, true);
	}

	/**
	 * <p>是否是本地IP地址</p>
	 * 
	 * @param host IP地址
	 * 
	 * @return true-是；false-不是；
	 * 
	 * TODO：IPv6
	 */
	public static final boolean localIPAddress(String host) {
//		return InetAddress.getByName(host).isSiteLocalAddress(); // 不能验证本地回环地址
		final long value = ipToLong(host);
		return
			(NATIVE_A_IP_BEGIN <= value && value <= NATIVE_A_IP_END) ||
			(NATIVE_B_IP_BEGIN <= value && value <= NATIVE_B_IP_END) ||
			(NATIVE_C_IP_BEGIN <= value && value <= NATIVE_C_IP_END) ||
			(NATIVE_L_IP_BEGIN <= value && value <= NATIVE_L_IP_END);
	}
	
	/**
	 * <p>创建Socket地址</p>
	 * 
	 * @param port 端口
	 * 
	 * @return Socket地址
	 */
	public static final InetSocketAddress buildSocketAddress(final int port) {
		return buildSocketAddress(null, port);
	}
	
	/**
	 * <p>创建Socket地址</p>
	 * 
	 * @param host 地址
	 * @param port 端口
	 * 
	 * @return Socket地址
	 */
	public static final InetSocketAddress buildSocketAddress(final String host, final int port) {
		if(StringUtils.isEmpty(host)) {
			return new InetSocketAddress(port);
		} else {
			return new InetSocketAddress(host, port);
		}
	}
	
}
