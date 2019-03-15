package com.acgist.snail.net.udp;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.DatagramChannel;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.util.Iterator;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicLong;

import com.acgist.snail.net.tracker.TrackerCoder;
import com.acgist.snail.protocol.torrent.bean.InfoHash;
import com.acgist.snail.system.exception.DownloadException;
import com.acgist.snail.utils.IoUtils;
import com.acgist.snail.utils.NetUtils;

public class UdpClient {

	public static void main(String[] args) throws IOException, DownloadException, InterruptedException {
		new UdpClient().connect();
	}
	
	public void connect() throws IOException, DownloadException, InterruptedException {
		CountDownLatch down = new CountDownLatch(1);
		SocketAddress address = new InetSocketAddress("explodie.org", 6969);
//		SocketAddress address = new InetSocketAddress("thetracker.org", 80);
//		SocketAddress address = new InetSocketAddress("exodus.desync.com", 6969);
		DatagramChannel channel = DatagramChannel.open();
		channel.bind(new InetSocketAddress(8888));
		AtomicLong id = new AtomicLong(0);
		channel.configureBlocking(false);
		Selector selector = Selector.open();
		channel.register(selector, SelectionKey.OP_READ);
//		channel.connect(new InetSocketAddress("localhost", 28888));
//		channel.connect(address);
		System.out.println("tracker udp");
//		int x = channel.write(buf);
//		System.out.println(x);
		new Thread(() -> {
			while(true) {
				ByteBuffer buf = null;
				int keysSelected = 0;
				try {
					keysSelected = selector.select();
				} catch (IOException e) {
					e.printStackTrace();
				}
				if(keysSelected > 0) {
					System.out.println("读取返回信息");
					final Set<SelectionKey> selectedKeys = selector.selectedKeys();
					final Iterator<SelectionKey> selectedKeysIterator = selectedKeys.iterator();
					while(selectedKeysIterator.hasNext()) {
						final SelectionKey selectedKey = selectedKeysIterator.next();
						selectedKeysIterator.remove();
						
						if(selectedKey.isValid() && selectedKey.isReadable()) {
							buf = ByteBuffer.allocate(1024);
							SocketAddress bytesRead = null;
							try {
								bytesRead = channel.receive(buf);
							} catch (IOException e) {
								e.printStackTrace();
							}
							System.out.println("back：" + bytesRead);
							int size = buf.position();
							System.out.println("长度：" + size);
							buf.flip();
							if(id.get() == 0) {
								System.out.println("连接返回：");
								System.out.println("action：" + buf.getInt());
								System.out.println("transaction_id：" + buf.getInt());
								long idx = buf.getLong();
								System.out.println("connection_id：" + idx);
								id.set(idx);
							} else {
								System.out.println("Peer返回：");
								System.out.println(buf.getInt());
								System.out.println(buf.getInt());
								System.out.println("时间间隔：" + buf.getInt());
								System.out.println("未完成Peer数量：" + buf.getInt()); // peer数量
								System.out.println("已完成Peer数量：" + buf.getInt()); // peer数量
								while(buf.position() < size) {
									int ip = buf.getInt();
									System.out.println(ip);
									System.out.println("Peer（IP:PORT）：" + NetUtils.intToIp(ip) + ":" + buf.getShort());
								}
							}
							buf.clear();
							down.countDown();
						}
					}
				} else {
					System.out.println("fail");
				}
			}
		}).start();
		InfoHash hash = InfoHash.newInstance("868f1199b18d05bf103aa8a8321f6428854d712e");
		ByteBuffer bBuffer = ByteBuffer.allocate(16);
		bBuffer.putLong(4497486125440L); // connection_id
		bBuffer.putInt(0);
		bBuffer.putInt(12345);
		bBuffer.flip();
		System.out.println("发送id");
//		channel.write(bBuffer);
		channel.send(bBuffer, address);
		down.await();
		bBuffer = ByteBuffer.allocate(98);
		bBuffer.putLong(id.get()); // connection_id
		bBuffer.putInt(1);
		bBuffer.putInt(12345);
		bBuffer.put(hash.hash());// <<<< what you asked.. adding the infoHash which is byte[]
		bBuffer.put("12345678901234567890".getBytes());
		bBuffer.putLong(0L);//download
		bBuffer.putLong(0L);//left
		bBuffer.putLong(0L);//uploaded
		bBuffer.putInt(2);//event
		bBuffer.putInt(0);// local ip
		bBuffer.putInt(0);//secret key
		bBuffer.putInt(50);//numwant
		bBuffer.putShort((short)8888);//numwant
		bBuffer.flip();
		System.out.println("发送hash");
//		channel.write(bBuffer);
		channel.send(bBuffer, address);
		System.out.println("OK");
	}
	
}
