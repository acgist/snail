package com.acgist.snail.net.udp;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.DatagramChannel;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.charset.StandardCharsets;
import java.util.Iterator;
import java.util.Set;

public class UdpClient {

	public static void main(String[] args) throws IOException {
		new UdpClient().connect();
	}
	
	/**
	 */
	public void connect() throws IOException {
		DatagramChannel channel = DatagramChannel.open();
		channel.bind(new InetSocketAddress(28888));
//		channel.configureBlocking(false);
		Selector selector = Selector.open();
//		channel.register(selector, SelectionKey.OP_READ);
		channel.connect(new InetSocketAddress("tracker.opentrackr.org", 1337));
		ByteBuffer buf = null;
		System.out.println("xx");
//		int x = channel.write(buf);
//		System.out.println(x);
		final ByteArrayOutputStream baos = new ByteArrayOutputStream();
		final DataOutputStream dos = new DataOutputStream(baos);
		dos.writeLong(2321312L);
		dos.writeInt(0);
		dos.writeLong(2321312L);
		// IP address (0 = default)
		buf = ByteBuffer.wrap(baos.toByteArray());
//		channel.write(buf);
//		channel.send(buf, new InetSocketAddress("tracker.opentrackr.org", 1337));
//		int bytesWritten = channel.write(buf);
		buf = ByteBuffer.allocate(1024);
		byte[] bytes = baos.toByteArray();
		System.out.println(new String(bytes));
		channel.socket().send(new DatagramPacket(bytes, bytes.length));
//		System.out.println("xx" + bytesWritten);
//		channel.send(buf, new InetSocketAddress("localhost", 28888));
		SocketAddress bytesRead = channel.receive(buf);
		System.out.println("xx" + bytesRead);
		System.out.println(new String(buf.array()));
//		final int keysSelected = selector.select();
//		if(keysSelected > 0) {
//			final Set<SelectionKey> selectedKeys = selector.selectedKeys();
//			final Iterator<SelectionKey> selectedKeysIterator = selectedKeys.iterator();
//			System.out.println("xxxx");
//			while(selectedKeysIterator.hasNext()) {
//				final SelectionKey selectedKey = selectedKeysIterator.next();
//				selectedKeysIterator.remove();
//				
//				if(selectedKey.isValid() && selectedKey.isReadable()) {
//					bytesRead = channel.receive(buf);
//					System.out.println("xddx" + bytesRead);
//				}
//			}
//		} else {
//			System.out.println("ccc");
//		}
	}
	
}
