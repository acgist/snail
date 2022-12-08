package com.acgist.snail.net.quick;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.nio.channels.WritableByteChannel;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

import com.acgist.snail.config.DownloadConfig;
import com.acgist.snail.config.QuickConfig;
import com.acgist.snail.config.QuickConfig.Type;
import com.acgist.snail.config.SystemConfig;
import com.acgist.snail.logger.Logger;
import com.acgist.snail.logger.LoggerFactory;
import com.acgist.snail.net.NetException;
import com.acgist.snail.net.UdpMessageHandler;
import com.acgist.snail.utils.ByteUtils;
import com.acgist.snail.utils.DateUtils;
import com.acgist.snail.utils.NetUtils;

/**
 * 快传消息代理
 * 
 * 0               8       12      16              24              32
 * +---------------+---------------+---------------+---------------+
 * | mark          | dir   | type  | rtt                           |
 * +---------------+---------------+---------------+---------------+
 * | timestamp                                                     |
 * +---------------+---------------+---------------+---------------+
 * | seq (req | ack)                                               |
 * +---------------+---------------+---------------+---------------+
 * | data                                                          |
 * +---------------+---------------+---------------+---------------+
 * 
 * TODO：混淆加密
 * 
 * @author acgist
 */
public class QuickMessageHandler extends UdpMessageHandler {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(QuickMessageHandler.class);
	
	private static final QuickMessageHandler INSTANCE = new QuickMessageHandler();
	
	public static final QuickMessageHandler getInstance() {
		return INSTANCE;
	}

	/**
	 * 默认窗口
	 */
	private volatile int wnd;
	/**
	 * 发送请求编号
	 */
	private volatile int sendSeq;
	/**
	 * 接收请求编号
	 */
	private volatile int recvSeq;
	/**
	 * 是否完成
	 */
	private volatile boolean finish;
	/**
	 * 是否连接
	 */
	private volatile boolean connect;
	/**
	 * 延迟时间
	 */
	private volatile short rtt;
	/**
	 * 文件大小
	 */
	private volatile long fileSize;
	/**
	 * 已经发送文件大小
	 */
	private volatile long sendSize;
	/**
	 * 已经接收文件大小
	 */
	private volatile long recvSize;
	/**
	 * 发送文件
	 */
	private File sendFile;
	/**
	 * 接收文件
	 */
	private File recvFile;
	/**
	 * <p>输入流</p>
	 */
	protected ReadableByteChannel input;
	/**
	 * <p>输出流</p>
	 */
	protected WritableByteChannel output;
	/**
	 * <p>窗口信号量</p>
	 */
	private final Semaphore semaphore;
	/**
	 * 临时缓存
	 */
	private final ByteBuffer byteBuffer;
	/**
	 * 数据发送存储
	 */
	private final Map<Integer, ByteBuffer> sendMap;
	/**
	 * 数据接收存储
	 */
	private final Map<Integer, ByteBuffer> recvMap;
	/**
	 * 成功候选
	 */
	private Candidate candidate;
	/**
	 * 成功候选地址
	 */
	private InetSocketAddress candidateSocketAddress;
	
	public QuickMessageHandler() {
		super(null);
		this.wnd = QuickConfig.DEFAULT_WND;
		this.sendSeq = 0;
		this.recvSeq = 0;
		this.finish = true;
		this.connect = false;
		this.rtt = 0;
		this.fileSize = 0L;
		this.sendSize = 0L;
		this.sendFile = null;
		this.recvFile = null;
		this.input = null;
		this.output = null;
		this.semaphore = new Semaphore(this.wnd);
		this.byteBuffer = ByteBuffer.allocateDirect(QuickConfig.PACKET_MAX_LENGTH - QuickConfig.HEADER_LENGTH - Long.BYTES);
		this.sendMap = new LinkedHashMap<>();
		this.recvMap = new LinkedHashMap<>();
	}

	/**
	 * 发送文件
	 * 
	 * @throws NetException 
	 */
	public boolean quick(File file) throws NetException {
		if(!this.finish) {
			return false;
		}
		this.finish = false;
		this.sendFile = file;
		if(this.sendFile == null || !this.sendFile.exists() || !this.sendFile.isFile()) {
			throw new NetException("文件无效：" + this.sendFile);
		}
		this.negotiate();
		while(!this.finish) {
			try {
				final boolean retry = !this.semaphore.tryAcquire(SystemConfig.RECEIVE_TIMEOUT, TimeUnit.SECONDS);
				if(this.finish) {
					break;
				}
				if(retry) {
					// 重发
					this.retry(this.sendSeq - 1, 16);
				} else {
					if(this.sendSize < this.fileSize) {
						// 发送
						this.transport();
					} else {
						// 重发
						this.retry(this.sendSeq - 1, 16);
						synchronized (this) {
							this.wait(SystemConfig.RECEIVE_TIMEOUT_MILLIS);
						}
					}
				}
			} catch (InterruptedException e) {
				Thread.currentThread().interrupt();
				LOGGER.debug("获取信号量异常", e);
			} catch (NetException e) {
				LOGGER.error("发送文件异常", e);
			}
			// 判断是否完成
			if(
				this.sendMap.size() <= 0 &&
				this.fileSize <= this.sendSize
			) {
				this.finish = true;
			}
		}
		return true;
	}
	
	@Override
	public void onReceive(ByteBuffer buffer, InetSocketAddress socketAddress) throws NetException {
		final byte mark = buffer.get();
		final byte dt = buffer.get();
		final short rtt = buffer.getShort();
		final int timestamp = buffer.getInt();
		final int seq = buffer.getInt();
		final byte dir = (byte) (dt >>> 4);
		final QuickConfig.Type type = QuickConfig.Type.of(dt);
		if(type == null) {
			LOGGER.warn("未知快传类型：{}-{}-{}", mark, dt);
		}
		if(dir == QuickConfig.DIRECTION_RES) {
			this.req(seq, rtt, timestamp);
			switch (type) {
			case CONNECT -> this.connectReq(seq, buffer, socketAddress);
			case NEGOTIATE -> this.negotiateReq(seq, buffer, socketAddress);
			case TRANSPORT -> this.transportReq(seq, buffer, socketAddress);
			}
		} else if(dir == QuickConfig.DIRECTION_ACK) {
			switch (type) {
			case CONNECT -> this.connectAck(buffer);
			case NEGOTIATE -> this.negotiateAck(buffer);
			case TRANSPORT -> this.transportAck(buffer);
			}
			this.ack(seq, rtt, timestamp);
		} else {
			LOGGER.warn("未知快传方向：{}", dir);
		}
	}
	
	private void req(int seq, short rtt, int timestamp) throws NetException {
		this.rtt = (short) Math.abs(DateUtils.timestampUs() - timestamp);
	}
	
	private void ack(int seq, short rtt, int timestamp) throws NetException {
		synchronized (this.sendMap) {
			Entry<Integer, ByteBuffer> entry;
			final var iterator = this.sendMap.entrySet().iterator();
			while(iterator.hasNext()) {
				entry = iterator.next();
				// 编号是否已经处理
				final int diff = seq - entry.getKey();
				if(diff >= 0) {
					LOGGER.debug("删除序号：{}-{}", seq, entry.getKey());
					// 删除已经响应数据
					iterator.remove();
				} else {
					break;
				}
			}
		}
		// 快速重传：等差重传
		final int diff = this.sendSeq - seq;
		if(diff < 4) {
			this.semaphore.release(1);
		} else if(diff < 32) {
			this.semaphore.release();
		} else {
			// 不再释放
			this.retry(seq, 4);
		}
		LOGGER.debug("当前序号：{}", this.semaphore.availablePermits());
	}

	/**
	 * 重发
	 * 
	 * @throws NetException 
	 */
	private void retry(int seq, int size) throws NetException {
		LOGGER.debug("开始重传：{}-{}-{}", seq, size, this.semaphore.availablePermits());
		for (int index = 1; index <= size; index++) {
			final ByteBuffer retry = this.sendMap.get(seq + index);
			if(retry == null) {
				return;
			}
			this.send(retry, this.candidateSocketAddress);
		}
	}

	/**
	 * @param type 类型
	 * @param bytes 数据
	 * 
	 * @return 请求
	 */
	public ByteBuffer newReq(QuickConfig.Type type, byte[] bytes) {
		LOGGER.debug("快传请求：{}-{}", type, this.sendSeq);
		final ByteBuffer buffer = ByteBuffer.allocate(QuickConfig.HEADER_LENGTH + bytes.length);
		buffer.put(QuickConfig.QUICK_HEADER);
		buffer.put(type.dt(QuickConfig.DIRECTION_RES));
		final int timestamp = DateUtils.timestampUs();
		buffer.putShort(this.rtt);
		buffer.putInt(timestamp);
		buffer.putInt(this.sendSeq);
		buffer.put(bytes);
		synchronized (this.sendMap) {
			this.sendMap.put(this.sendSeq, buffer);
		}
		synchronized (this) {
			this.sendSeq++;
		}
		return buffer;
	}
	
	/**
	 * @return 响应
	 */
	public ByteBuffer newAck(QuickConfig.Type type, int seq, byte[] bytes) {
		LOGGER.debug("快传响应：{}-{}", type, seq);
		final ByteBuffer buffer = ByteBuffer.allocate(QuickConfig.HEADER_LENGTH + bytes.length);
		buffer.put(QuickConfig.QUICK_HEADER);
		buffer.put(type.dt(QuickConfig.DIRECTION_ACK));
		final int timestamp = DateUtils.timestampUs();
		buffer.putShort(this.rtt);
		buffer.putInt(timestamp);
		buffer.putInt(seq);
		buffer.put(bytes);
		return buffer;
	}
	
	/**
	 * 0               8               16              24              32
	 * +---------------+---------------+---------------+---------------+
	 * | candidate length                                              |
	 * +---------------+---------------+---------------+---------------+
	 * | candidate ...
	 * +---------------+---------------+---------------+---------------+
	 * 
	 * @throws NetException 
	 */
	public boolean connect(Candidate candidate) throws NetException {
		final byte[] bytes = candidate.toString().getBytes();
		final int candidateLength = bytes.length;
		final ByteBuffer buffer = ByteBuffer.allocate(Integer.BYTES + candidateLength);
		buffer.putInt(candidateLength);
		buffer.put(bytes);
		this.send(this.newReq(Type.CONNECT, buffer.array()), NetUtils.buildSocketAddress(candidate.getHost(), candidate.getPort()));
		synchronized (this) {
			try {
				this.wait(SystemConfig.CONNECT_TIMEOUT_MILLIS);
			} catch (InterruptedException e) {
				Thread.currentThread().interrupt();
				LOGGER.debug("快传等待连接异常", e);
			}
		}
		return this.connect;
	}
	
	private void connectReq(int seq, ByteBuffer buffer, InetSocketAddress socketAddress) throws NetException {
		this.send(this.newAck(Type.CONNECT, seq, ByteUtils.remainingToBytes(buffer)), socketAddress);
	}
	
	private void connectAck(ByteBuffer buffer) {
		this.connect = true;
		this.candidate = Candidate.of(ByteUtils.remainingToString(buffer));
		this.candidateSocketAddress = NetUtils.buildSocketAddress(this.candidate.getHost(), this.candidate.getPort());
		LOGGER.debug("快传候选地址：{}", this.candidate);
		synchronized (this) {
			this.notifyAll();
		}
	}
	
	/**
	 * 同时传送多文件时添加sessionId解决
	 * 
	 * 0               8               16              24              32
	 * +---------------+---------------+---------------+---------------+
	 * | file size (64 bits)                                           |
	 * +---------------+---------------+---------------+---------------+
	 * | recv size (64 bits)                                           |
	 * +---------------+---------------+---------------+---------------+
	 * | file name length                                              |
	 * +---------------+---------------+---------------+---------------+
	 * | file name ...
	 * +---------------+---------------+---------------+---------------+
	 * 
	 * @throws NetException 
	 */
	public void negotiate() throws NetException {
		this.send(this.newReq(Type.NEGOTIATE, this.buildNegotiate(this.sendFile.length(), 0, this.sendFile.getName())), this.candidateSocketAddress);
	}
	
	private void negotiateReq(int seq, ByteBuffer buffer, InetSocketAddress socketAddress) throws NetException {
		this.fileSize = buffer.getLong();
		long recvSize = buffer.getLong();
		final int fileNameLength = buffer.getInt();
		final byte[] bytes = new byte[fileNameLength];
		buffer.get(bytes);
		final String fileName = new String(bytes);
		this.recvFile = new File(DownloadConfig.getPath(fileName));
		if(this.recvFile.exists()) {
			recvSize = this.recvFile.length();
		}
		try {
			final OutputStream outputStream;
			if(recvSize > 0L) {
				outputStream = new FileOutputStream(this.recvFile, true);
			} else {
				outputStream = new FileOutputStream(this.recvFile);
			}
			final BufferedOutputStream bufferedOutputStream = new BufferedOutputStream(outputStream, DownloadConfig.getMemoryBufferByte());
			this.output = Channels.newChannel(bufferedOutputStream);
		} catch (FileNotFoundException e) {
			throw new NetException("打开文件失败", e);
		}
		synchronized (this) {
			this.recvSeq = seq + 1;
		}
		this.send(this.newAck(Type.NEGOTIATE, seq, this.buildNegotiate(this.fileSize, recvSize, fileName)), socketAddress);
	}
	
	private void negotiateAck(ByteBuffer buffer) throws NetException {
		final long fileSize = buffer.getLong();
		final long recvSize = buffer.getLong();
		if(recvSize == fileSize) {
			this.finish = true;
			this.semaphore.release();
			LOGGER.debug("任务已经完成：{}-{}-{}", this.sendFile, fileSize, recvSize);
		}
		this.sendSize = recvSize;
		try {
			final InputStream inputStream = new FileInputStream(this.sendFile);
			final BufferedInputStream bufferedInputStream = new BufferedInputStream(inputStream, DownloadConfig.getMemoryBufferByte());
			bufferedInputStream.skip(this.sendSize);
			this.input = Channels.newChannel(bufferedInputStream);
		} catch (IOException e) {
			throw new NetException("文件定位错误：" + this.sendSize, e);
		}
		LOGGER.debug("快传文件协商：{}-{}", fileSize, recvSize);
		synchronized (this) {
			this.notifyAll();
		}
	}
	
	private byte[] buildNegotiate(long fileSize, long recvSize, String fileName) {
		final byte[] bytes = fileName.getBytes();
		final int fileNameLength = bytes.length;
		final ByteBuffer buffer = ByteBuffer.allocate(Long.BYTES + Long.BYTES + Integer.BYTES + fileNameLength);
		buffer.putLong(fileSize);
		buffer.putLong(recvSize);
		buffer.putInt(fileNameLength);
		buffer.put(bytes);
		return buffer.array();
	}
	
	/**
	 * 0               8               16              24              32
	 * +---------------+---------------+---------------+---------------+
	 * | data length (32 bits)                                         |
	 * +---------------+---------------+---------------+---------------+
	 * | data ...
	 * +---------------+---------------+---------------+---------------+
	 * 
	 * @throws NetException 
	 */
	public void transport() throws NetException {
		final byte[] bytes;
		try {
			this.byteBuffer.putInt(0);
			final int size = this.input.read(this.byteBuffer);
			if(size <= 0) {
				LOGGER.debug("快传文件传输完成：{}", size);
				return;
			}
			this.byteBuffer.putInt(0, size);
			synchronized (this) {
				this.sendSize += size;
			}
			this.byteBuffer.flip();
			bytes = ByteUtils.remainingToBytes(this.byteBuffer);
			this.byteBuffer.clear();
		} catch (IOException e) {
			throw new NetException("文件读取失败", e);
		}
		this.send(this.newReq(Type.TRANSPORT, bytes), this.candidateSocketAddress);
	}
	
	public void transportReq(int seq, ByteBuffer buffer, InetSocketAddress socketAddress) throws NetException {
		if(seq < this.recvSeq) {
			LOGGER.debug("已经过期数据：{}-{}", seq, this.recvSeq);
			return;
		}
		this.recvMap.put(seq, buffer);
		int recvSeq = this.recvSeq;
		while(true) {
			final ByteBuffer local;
			synchronized (this.recvMap) {
				local = this.recvMap.remove(recvSeq + 1);
			}
			if(local == null) {
				break;
			} else {
				recvSeq = recvSeq + 1;
			}
			final int size = local.getInt();
			synchronized (this) {
				this.recvSize += size;
			}
			try {
				this.output.write(local);
			} catch (IOException e) {
				throw new NetException("文件写入失败：" + size, e);
			}
		}
		if(this.fileSize <= recvSize) {
			this.finish = true;
			this.semaphore.release();
		}
		synchronized (this) {
			this.recvSeq = Math.max(this.recvSeq, recvSeq);
		}
		this.send(this.newAck(Type.TRANSPORT, this.recvSeq, new byte[0]), socketAddress);
	}
	
	public void transportAck(ByteBuffer buffer) {
	}
	
	@Override
	public void close() {
		super.close();
		this.release();
	}
	
	public void release() {
		this.wnd = QuickConfig.DEFAULT_WND;
		this.sendSeq = 0;
		this.recvSeq = 0;
		this.finish = true;
		this.connect = false;
		this.rtt = 0;
		this.fileSize = 0L;
		this.sendSize = 0L;
		this.sendFile = null;
		this.recvFile = null;
		if(this.input != null) {
			try {
				this.input.close();
			} catch (IOException e) {
				LOGGER.error("释放文件连接异常", e);
			}
		}
		this.input = null;
		if(this.output != null) {
			try {
				this.output.close();
			} catch (IOException e) {
				LOGGER.error("释放文件连接异常", e);
			}
		}
		this.output = null;
		this.semaphore.drainPermits();
		this.semaphore.release(this.wnd);
		this.byteBuffer.clear();
		this.sendMap.clear();
		this.recvMap.clear();
	}
	
}
