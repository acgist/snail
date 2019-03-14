package com.acgist.main;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.DatagramChannel;
import java.util.Scanner;

import org.junit.Test;

public class UdpTest {

	@Test
	public void test() throws InterruptedException {
		server();
		Thread.sleep(60L * 60 * 1000);
	}
	
	public static final void server() {
		new Thread(new Runnable() {
			@Override
			public void run() {
				try {
					final DatagramChannel channel = DatagramChannel.open();
					channel.bind(new InetSocketAddress(28888));
					ByteBuffer buf = ByteBuffer.allocate(1024);
					while(true) {
						final SocketAddress address = channel.receive(buf);
						System.out.println(address);
						System.out.println("读取信息：" + new String(buf.array(), 0, buf.position()));
						buf.clear();
						new Thread(new Runnable() {
							@Override
							public void run() {
								try(Scanner scanner = new Scanner(System.in)) {
									while(scanner.hasNext()) {
										ByteBuffer buf = ByteBuffer.wrap(scanner.next().getBytes());
										System.out.println(new String(buf.array()));
										channel.send(buf, address);
										buf.clear();
									}
								} catch (IOException e) {
									e.printStackTrace();
								}
							}
						}).start();
					}
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}).start();
	}
	
}
