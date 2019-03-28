package com.acgist.main;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.MulticastSocket;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.StandardProtocolFamily;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.nio.channels.DatagramChannel;
import java.util.Scanner;

import org.junit.Test;

public class PortMappingTest {

	@Test
	public void MSEARCH() throws UnknownHostException, SocketException, IOException {
		final MulticastSocket socket = join("239.255.255.250", 1900);
		new Thread(new Runnable() {
			@Override
			public void run() {
//				M-SEARCH * HTTP/1.1
//				MX: 3
//				MAN: "ssdp:discover"
//				HOST: 239.255.255.250:1900
//				ST: upnp:rootdevice
				sendData(socket, (
					"M-SEARCH * HTTP/1.1\r\n" +
					"HOST: 239.255.255.250:1900\r\n" +
//					"ST: upnp:rootdevice\r\n" +
					"ST: urn:schemas-upnp-org:device:InternetGatewayDevice:1\r\n" +
					"MAN: \"ssdp:discover\"\r\n" +
					"MX: 3\r\n" +
					"\r\n"
					).getBytes(), "239.255.255.250");
				while(true) {
					Scanner scanner = new Scanner(System.in);
					sendData(socket, scanner.nextLine().getBytes(), "239.255.255.250");
				}
			}
		}).start();
		while(true) {
			String message = recieveData(socket, "239.255.255.250");
			System.out.println("接收数据：\r\n" + message);
		}
	}

	public static MulticastSocket join(String group, int port) {
		try {
			MulticastSocket socket = new MulticastSocket(8888);
			socket.setTimeToLive(2);
			socket.setSoTimeout(5 * 1000);
			socket.joinGroup(InetAddress.getByName(group));
			return socket;
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	public static void sendData(MulticastSocket socket, byte[] data, String group) {
		try {
			System.out.println("发送数据：\r\n" + new String(data));
			DatagramPacket packet = new DatagramPacket(data, 0, data.length, InetAddress.getByName(group), 1900);
			socket.send(packet);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static String recieveData(MulticastSocket socket, String group) {
		String message = null;
		try {
			byte[] data = new byte[1024];
			DatagramPacket packet = new DatagramPacket(data, 0, data.length, InetAddress.getByName(group), 1900);
			socket.receive(packet); // 通过MulticastSocket实例端口从组播组接收数据
			// 将接受的数据转换成字符串形式
			message = new String(packet.getData());
		} catch (Exception e) {
			e.printStackTrace();
		}
		return message;
	}

}
