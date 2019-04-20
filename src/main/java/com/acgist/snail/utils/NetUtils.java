package com.acgist.snail.utils;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * utils - net
 */
public class NetUtils {

	private static final Logger LOGGER = LoggerFactory.getLogger(NetUtils.class);
	
	// A类私用地址
	private static final long A_IP_BEGIN = decodeIpToLong("10.0.0.0");
	private static final long A_IP_END = decodeIpToLong("10.255.255.255");
	// B类私用地址
	private static final long B_IP_BEGIN = decodeIpToLong("172.16.0.0");
	private static final long B_IP_END = decodeIpToLong("172.31.255.255");
	// C类私用地址
	private static final long C_IP_BEGIN = decodeIpToLong("192.168.0.0");
	private static final long C_IP_END = decodeIpToLong("192.168.255.255");
	// 系统环回地址
	private static final long L_IP_BEGIN = decodeIpToLong("127.0.0.0");
	private static final long L_IP_END = decodeIpToLong("127.255.255.255");

	public static final int decodePort(short port) {
		return Short.toUnsignedInt(port);
	}
	
	public static final short encodePort(int port) {
		return (short) port;
	}
	
	public static final int decodeIpToInt(String ipAddress) {
		return (int) decodeIpToLong(ipAddress);
	}
	
	public static final long decodeIpToLong(String ipAddress) {
		long result = 0, tmp;
		final String[] array = ipAddress.split("\\.");
		for (int index = 3; index >= 0; index--) {
			tmp = Long.parseLong(array[3 - index]);
			result |= tmp << (index * 8);
		}
		return result;
	}

	public static final String encodeIntToIp(int ipNumber) {
		return encodeLongToIp(Integer.toUnsignedLong(ipNumber));
	}
	
	public static final String encodeLongToIp(long ipNumber) {
		return ((ipNumber >> 24) & 0xFF) + "." + ((ipNumber >> 16) & 0xFF) + "." + ((ipNumber >> 8) & 0xFF) + "." + (ipNumber & 0xFF);
	}
	
	/**
	 * 获取本机名称
	 * TODO：初始化一次
	 */
	public static final String inetHostName() {
		try {
			InetAddress address = InetAddress.getLocalHost();
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
			InetAddress address = InetAddress.getLocalHost();
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
	 * 判断是否是本地IP
	 */
	public static final boolean isLocalIp(String ipAddress) {
		final long value = decodeIpToLong(ipAddress);
		return
			(A_IP_BEGIN < value && value < A_IP_END) ||
			(B_IP_BEGIN < value && value < B_IP_END) ||
			(C_IP_BEGIN < value && value < C_IP_END) ||
			(L_IP_BEGIN < value && value < L_IP_END);
	}

}
