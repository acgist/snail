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
	
	/**
	 * IP转int
	 */
	public static final long ipToInt(String address) {
		long result = 0;
		final String[] array = address.split("\\.");
		for (int i = 3; i >= 0; i--) {
			long ip = Long.parseLong(array[3 - i]);
			result |= ip << (i * 8);
		}
		return result;
	}

	/**
	 * int转IP
	 */
	public static final String intToIp(int ipNumber) {
		return ((ipNumber >> 24) & 0xFF) + "." + ((ipNumber >> 16) & 0xFF) + "." + ((ipNumber >> 8) & 0xFF) + "." + (ipNumber & 0xFF);
	}
	
	/**
	 * 获取本机名称
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

}
