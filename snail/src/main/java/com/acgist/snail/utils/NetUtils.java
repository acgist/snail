package com.acgist.snail.utils;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.StandardProtocolFamily;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.Objects;
import java.util.StringTokenizer;
import java.util.concurrent.atomic.AtomicInteger;

import com.acgist.snail.config.SymbolConfig.Symbol;
import com.acgist.snail.config.SystemConfig;
import com.acgist.snail.logger.Logger;
import com.acgist.snail.logger.LoggerFactory;

/**
 * <p>网络工具</p>
 * 
 * @author acgist
 */
public final class NetUtils {

	private static final Logger LOGGER = LoggerFactory.getLogger(NetUtils.class);
	
	/**
	 * <p>本机名称</p>
	 * <p>例子：acgist</p>
	 */
	public static final String LOCAL_HOST_NAME;
	/**
	 * <p>本机地址</p>
	 * <p>例子：192.168.1.100</p>
	 */
	public static final String LOCAL_HOST_ADDRESS;
	/**
	 * <p>本机子网前缀</p>
	 */
	public static final short LOCAL_PREFIX_LENGTH;
	/**
	 * <p>本机环回名称</p>
	 * <p>例子：localhost</p>
	 */
	public static final String LOOPBACK_HOST_NAME;
	/**
	 * <p>本机环回地址</p>
	 * <p>例子：127.0.0.1</p>
	 */
	public static final String LOOPBACK_HOST_ADDRESS;
	/**
	 * <p>本机默认物理网卡</p>
	 */
	public static final NetworkInterface DEFAULT_NETWORK_INTERFACE;
	/**
	 * <p>本机IP地址协议</p>
	 */
	public static final StandardProtocolFamily LOCAL_PROTOCOL_FAMILY;
	/**
	 * <p>最大端口号：{@value}</p>
	 */
	public static final int MAX_PORT = 2 << 15;
	/**
	 * <p>相同区域IP差距：{@value}</p>
	 */
	private static final int AREA_IP = 1 << 8;
	/**
	 * <p>IPv4地址正则表达式：{@value}</p>
	 */
	private static final String IPV4_REGEX = "(\\d{1,3}\\.){3}\\d{1,3}";
	/**
	 * <p>IPv6地址正则表达式：{@value}</p>
	 */
	private static final String IPV6_REGEX =
		// IPv6
		"((([0-9a-f]{1,4}(:|::))|(::)){0,7}){1}" +
		// IPv6 + IPv4
		"(([0-9a-f]{1,4})|(\\d{1,3}\\.){3}\\d{1,3})?" +
		// 子网前缀
		"(/\\d{0,3})?" +
		// 网卡标识
		"(%.+)?";
	
	static {
		final AtomicInteger index = new AtomicInteger(Integer.MAX_VALUE);
		final ModifyOptional<Short> localPrefixLength = ModifyOptional.newInstance();
		final ModifyOptional<String> localHostAddress = ModifyOptional.newInstance();
		final ModifyOptional<NetworkInterface> defaultNetworkInterface = ModifyOptional.newInstance();
		try {
			// 处理多个物理网卡和虚拟网卡
			NetworkInterface.networkInterfaces().filter(NetUtils::available).forEach(networkInterface -> {
				final int nowIndex = networkInterface.getIndex();
				networkInterface.getInterfaceAddresses().forEach(interfaceAddress -> {
					// 本机地址
					final var address = interfaceAddress.getAddress();
					// 本地地址和公网地址
					if(
						// 索引最小网卡
						index.get() > nowIndex &&
						// 本地地址：A/B/C类本地地址
//						address.isSiteLocalAddress() &&
						// 通配地址
						!address.isAnyLocalAddress() &&
						// 环回地址
						!address.isLoopbackAddress() &&
						// 链接地址：虚拟网卡
						!address.isLinkLocalAddress() &&
						// 组播地址
						!address.isMulticastAddress()
					) {
						index.set(nowIndex);
//						address.getHostName() // 速度太慢：buildLocalHostName()
						localHostAddress.set(address.getHostAddress());
						localPrefixLength.set(interfaceAddress.getNetworkPrefixLength());
						defaultNetworkInterface.set(networkInterface);
					}
				});
			});
		} catch (SocketException e) {
			LOGGER.error("初始化本机网络信息异常", e);
		}
		LOCAL_HOST_NAME = buildLocalHostName();
		LOCAL_HOST_ADDRESS = localHostAddress.get();
		LOCAL_PREFIX_LENGTH = localPrefixLength.get((short) 0);
		LOOPBACK_HOST_NAME = buildLoopbackHostName();
		LOOPBACK_HOST_ADDRESS = buildLoopbackHostAddress();
		DEFAULT_NETWORK_INTERFACE = defaultNetworkInterface.get();
		LOCAL_PROTOCOL_FAMILY = ipv4(LOCAL_HOST_ADDRESS) ? StandardProtocolFamily.INET : StandardProtocolFamily.INET6;
		LOGGER.debug("本机名称：{}", LOCAL_HOST_NAME);
		LOGGER.debug("本机地址：{}", LOCAL_HOST_ADDRESS);
		LOGGER.debug("本机子网前缀：{}", LOCAL_PREFIX_LENGTH);
		LOGGER.debug("本机环回名称：{}", LOOPBACK_HOST_NAME);
		LOGGER.debug("本机环回地址：{}", LOOPBACK_HOST_ADDRESS);
		LOGGER.debug("本机默认物理网卡：{}", DEFAULT_NETWORK_INTERFACE);
		LOGGER.debug("本机IP地址协议：{}", LOCAL_PROTOCOL_FAMILY);
	}
	
	private NetUtils() {
	}

	/**
	 * <p>判断网卡是否有效（排除：启动状态、虚拟网卡、环回地址等等）</p>
	 * 
	 * @param networkInterface 网卡
	 * 
	 * @return 是否有效
	 */
	public static final boolean available(NetworkInterface networkInterface) {
		try {
			return
				// 启动状态
				networkInterface.isUp() &&
				// 虚拟网卡
				!networkInterface.isVirtual() &&
				// 环回地址
				!networkInterface.isLoopback() &&
				// 点对点网卡
				!networkInterface.isPointToPoint();
		} catch (SocketException e) {
			LOGGER.error("获取网卡状态异常", e);
		}
		return false;
	}
	
	/**
	 * <p>判断本地IP是否是IPv4</p>
	 * 
	 * @return 本地IP是否是IPv4
	 */
	public static final boolean localIPv4() {
		return LOCAL_PROTOCOL_FAMILY == StandardProtocolFamily.INET;
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
	 * <p>IPv4地址编码</p>
	 * 
	 * @param ip IP地址（字符串）
	 * 
	 * @return IP地址（int）
	 */
	public static final int ipToInt(String ip) {
		Objects.requireNonNull(ip, "IP地址不能为空");
		final StringTokenizer tokenizer = new StringTokenizer(ip, Symbol.DOT.toString());
		int index = 0;
		final byte[] bytes = new byte[4];
		while(tokenizer.hasMoreTokens()) {
			if(bytes.length <= index) {
				throw new IllegalArgumentException("IP地址错误：" + ip);
			}
			bytes[index++] = (byte) Short.parseShort(tokenizer.nextToken());
		}
		return NumberUtils.bytesToInt(bytes);
	}
	
	/**
	 * <p>IPv4地址解码</p>
	 * 
	 * @param ip IP地址（int）
	 * 
	 * @return IP地址（字符串）
	 */
	public static final String intToIP(int ip) {
		final byte[] bytes = NumberUtils.intToBytes(ip);
		final StringBuilder builder = new StringBuilder();
		builder
			.append(Byte.toUnsignedInt(bytes[0])).append(Symbol.DOT.toString())
			.append(Byte.toUnsignedInt(bytes[1])).append(Symbol.DOT.toString())
			.append(Byte.toUnsignedInt(bytes[2])).append(Symbol.DOT.toString())
			.append(Byte.toUnsignedInt(bytes[3]));
		return builder.toString();
	}
	
	/**
	 * <p>IP地址编码</p>
	 * <p>支持IP：IPv4、IPv6</p>
	 * 
	 * @param ip IP地址（字符串）
	 * 
	 * @return IP地址（字节数组）
	 */
	public static final byte[] ipToBytes(String ip) {
		try {
			return InetAddress.getByName(ip).getAddress();
		} catch (UnknownHostException e) {
			LOGGER.error("IP地址编码异常：{}", ip, e);
		}
		return null;
	}
	
	/**
	 * <p>IP地址解码</p>
	 * <p>支持IP：IPv4、IPv6</p>
	 * 
	 * @param value IP地址（字节数组）
	 * 
	 * @return IP地址（字符串）
	 */
	public static final String bytesToIP(byte[] value) {
		if(value == null) {
			return null;
		}
		try {
			return InetAddress.getByAddress(value).getHostAddress();
		} catch (UnknownHostException e) {
			LOGGER.error("IP地址解码异常", e);
		}
		return null;
	}
	
	/**
	 * <p>读取IPv6</p>
	 * 
	 * @param buffer 数据
	 * 
	 * @return IPv6
	 */
	public static final byte[] bufferToIPv6(ByteBuffer buffer) {
		if(buffer == null) {
			return null;
		}
		final byte[] bytes = new byte[SystemConfig.IPV6_LENGTH];
		buffer.get(bytes);
		return bytes;
	}
	
	/**
	 * <p>判断是否是IP地址</p>
	 * 
	 * @param host IP地址
	 * 
	 * @return 是否是IP地址
	 * 
	 * @see #ipv4(String)
	 * @see #ipv6(String)
	 */
	public static final boolean ip(String host) {
		return ipv4(host) || ipv6(host);
	}
	
	/**
	 * <p>判断是否是IPv4地址</p>
	 * 
	 * @param host IP地址
	 * 
	 * @return 是否是IPv4地址
	 */
	public static final boolean ipv4(String host) {
		return StringUtils.regex(host, IPV4_REGEX, true);
	}
	
	/**
	 * <p>判断是否是IPv6地址</p>
	 * 
	 * @param host IP地址
	 * 
	 * @return 是否是IPv6地址
	 */
	public static final boolean ipv6(String host) {
		return StringUtils.regex(host, IPV6_REGEX, true);
	}
	
	/**
	 * <p>判断是否是同个局域网</p>
	 * 
	 * @param host 地址
	 * 
	 * @return 是否是同个局域网
	 */
	public static final boolean lan(String host) {
		if(ip(host)) {
			final byte[] bytes = ipToBytes(host);
			final byte[] localHostBytes = ipToBytes(LOCAL_HOST_ADDRESS);
			final int index = Arrays.mismatch(bytes, localHostBytes);
			if(index == -1) {
				// 完全匹配
				return true;
			}
			// 每个字节八位
			return index * 8 >= LOCAL_PREFIX_LENGTH;
		}
		return false;
	}
	
	/**
	 * <p>判断是否是本地IP地址</p>
	 * <p>A类私用地址</p>
	 * <p>A类地址范围：0.0.0.0-127.255.255.255</p>
	 * <p>A类默认子网掩码：255.0.0.0</p>
	 * <p>B类私用地址</p>
	 * <p>B类地址范围：128.0.0.0-191.255.255.255</p>
	 * <p>B类默认子网掩码：255.255.0.0</p>
	 * <p>C类私用地址</p>
	 * <p>C类地址范围：192.0.0.0-223.255.255.255</p>
	 * <p>C类默认子网掩码：255.255.255.0</p>
	 * <p>本地环回地址</p>
	 * <p>本地环回地址范围：127.0.0.0-127.255.255.255</p>
	 * <p>DHCP地址</p>
	 * <p>DHCP地址范围：169.254.0.0-169.254.255.255</p>
	 * <p>组播地址</p>
	 * <p>组播地址范围：224.0.0.0-239.255.255.255</p>
	 * 
	 * @param host 地址
	 * 
	 * @return 是否是本地IP地址
	 */
	public static final boolean localIP(String host) {
		InetAddress inetAddress = null;
		try {
			inetAddress = InetAddress.getByName(host);
		} catch (UnknownHostException e) {
			LOGGER.error("IP地址转换异常：{}", host, e);
			return true;
		}
		return
			// 通配地址
			inetAddress.isAnyLocalAddress() ||
			// 环回地址
			inetAddress.isLoopbackAddress() ||
			// 链接地址：虚拟网卡
			inetAddress.isLinkLocalAddress() ||
			// 组播地址
			inetAddress.isMulticastAddress() ||
			// 本地地址：A/B/C类本地地址
			inetAddress.isSiteLocalAddress();
	}
	
	/**
	 * <p>判断是否相同区域IP</p>
	 * 
	 * @param source 原始IP
	 * @param target 目标IP
	 * 
	 * @return 是否相同区域IP
	 */
	public static final boolean areaIP(String source, String target) {
		final int sourceIp = NetUtils.ipToInt(source);
		final int targetIp = NetUtils.ipToInt(target);
		return (sourceIp ^ targetIp) <= AREA_IP;
	}
	
	/**
	 * <p>新建Socket地址</p>
	 * 
	 * @param port 端口
	 * 
	 * @return Socket地址
	 */
	public static final InetSocketAddress buildSocketAddress(final int port) {
		return buildSocketAddress(null, port);
	}
	
	/**
	 * <p>新建Socket地址</p>
	 * <p>注意：如果HOST是域名可能出现DNS查询超时</p>
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
	 * <p>获取本机环回名称</p>
	 * 
	 * @return 本机环回名称
	 */
	private static final String buildLoopbackHostName() {
		return InetAddress.getLoopbackAddress().getHostName();
	}
	
	/**
	 * <p>获取本机环回地址</p>
	 * 
	 * @return 本机环回地址
	 */
	private static final String buildLoopbackHostAddress() {
		return InetAddress.getLoopbackAddress().getHostAddress();
	}
	
}
