package com.acgist.snail.net.udp;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.DatagramChannel;
import java.util.Scanner;

public class UdpServer {

	public static void main(String[] args) {
		server();
		client();
	}
	
	public static final void server() {
		new Thread(() -> {
			DatagramChannel channel;
			try {
				channel = DatagramChannel.open();
				channel.bind(new InetSocketAddress(28888));
				ByteBuffer buf = ByteBuffer.allocate(1024);
				while(true) {
					channel.receive(buf);
					System.out.println("++++++++++++" + new String(buf.array(), 0, buf.position()));
					buf.clear();
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}).start();
	}
	
	public static final void client() {
		new Thread(() -> {
			DatagramChannel channel;
			try {
				channel = DatagramChannel.open();
				Scanner scanner = new Scanner(System.in);
				while(scanner.hasNext()) {
					ByteBuffer buf = ByteBuffer.wrap(scanner.next().getBytes());
					System.out.println(new String(buf.array()));
					channel.send(buf, new InetSocketAddress("localhost", 28888));
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}).start();
	}
	
}
