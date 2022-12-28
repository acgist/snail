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
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Consumer;

import com.acgist.snail.config.DownloadConfig;
import com.acgist.snail.config.QuickConfig;
import com.acgist.snail.config.QuickConfig.Type;
import com.acgist.snail.config.SystemConfig;
import com.acgist.snail.logger.Logger;
import com.acgist.snail.logger.LoggerFactory;
import com.acgist.snail.net.NetException;
import com.acgist.snail.net.UdpMessageHandler;
import com.acgist.snail.utils.ByteUtils;
import com.acgist.snail.utils.IoUtils;
import com.acgist.snail.utils.NetUtils;

/**
 * 快传消息代理
 * 
 * 0               8       12      16              24              32
 * +---------------+-------+-------+---------------+---------------+
 * | mark          | dir   | type  | sessionId                     |
 * +---------------+-------+-------+---------------+---------------+
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
	
	/**
	 * 发送编号
	 */
	private volatile int sendSeq;
	/**
	 * 接收编号
	 */
	private volatile int recvSeq;
	/**
	 * 重传编号
	 */
	private volatile int retrySeq;
	/**
	 * 重传时间
	 */
	private volatile byte retryTimes;
	/**
	 * 上次消息时间戳
	 */
	private volatile long timestamp;
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
	 * 是否完成
	 */
	private volatile boolean finish;
	/**
	 * 是否协商
	 */
	private volatile boolean negotiate;
	/**
	 * 是否连接
	 */
	private volatile boolean connect;
	/**
	 * 发送文件
	 */
	private File sendFile;
	/**
	 * 接收文件
	 */
	private File recvFile;
	/**
	 * 成功候选
	 */
	private Candidate candidate;
	/**
	 * 进度
	 */
	private volatile double progress;
	/**
	 * 进度通知
	 */
	private Consumer<Double> consumer;
	/**
	 * <p>输入流</p>
	 */
	private ReadableByteChannel input;
	/**
	 * <p>输出流</p>
	 */
	private WritableByteChannel output;
	/**
	 * 成功候选地址
	 */
	private InetSocketAddress candidateSocketAddress;
	/**
	 * 上下文键
	 */
	private final String key;
	/**
	 * 会话ID
	 */
	private final short sessionId;
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
	 * 上下文
	 */
	private final QuickContext context;
	/**
	 * 锁
	 */
	private final Lock lock;
	/**
	 * 完成条件
	 */
	private final Condition finishCondition;
	/**
	 * 连接条件
	 */
	private final Condition connectCondition;
	/**
	 * 协商条件
	 */
	private final Condition negotiateCondition;
	
	public QuickMessageHandler(String key, short sessionId) {
		this(key, sessionId, null);
	}
	
	public QuickMessageHandler(String key, short sessionId, InetSocketAddress socketAddress) {
		super(socketAddress);
		this.sendSeq = 0;
		this.recvSeq = 0;
		this.timestamp = System.currentTimeMillis();
		this.fileSize = 0L;
		this.sendSize = 0L;
		this.recvSize = 0L;
		this.finish = false;
		this.connect = false;
		this.key = key;
		this.sessionId = sessionId;
		this.semaphore = new Semaphore(QuickConfig.DEFAULT_SEMAPHORE);
		this.byteBuffer = ByteBuffer.allocateDirect(QuickConfig.PACKET_MAX_LENGTH - QuickConfig.HEADER_LENGTH - Integer.BYTES);
		this.sendMap = new LinkedHashMap<>();
		this.recvMap = new LinkedHashMap<>();
		this.context = QuickContext.getInstance();
		this.lock = new ReentrantLock();
		this.finishCondition = this.lock.newCondition();
		this.connectCondition = this.lock.newCondition();
		this.negotiateCondition = this.lock.newCondition();
	}

	/**
	 * 发送文件
	 * 
	 * @param file 文件
	 * @param consumer 进度通知
	 * 
	 * @throws NetException 
	 */
	public boolean quick(File file, Consumer<Double> consumer) throws NetException {
		if(this.sendFile != null) {
			return false;
		}
		this.sendFile = file;
		this.consumer = consumer;
		if(this.sendFile == null || !this.sendFile.exists() || !this.sendFile.isFile()) {
			throw new NetException("文件无效：" + this.sendFile);
		}
		while(!this.close && !this.negotiate) {
			this.negotiate();
		}
		while(!this.close && !this.finish) {
			this.quick();
		}
		return true;
	}
	
	@Override
	public void onReceive(ByteBuffer buffer, InetSocketAddress socketAddress) throws NetException {
		final byte mark = buffer.get();
		final byte dt = buffer.get();
		final short sessionId = buffer.getShort();
		final int seq = buffer.getInt();
		final byte dir = (byte) (dt >>> 4);
		final QuickConfig.Type type = QuickConfig.Type.of(dt);
		if(type == null) {
			LOGGER.warn("未知快传类型：{}-{}-{}", mark, dt, sessionId);
		}
		if(dir == QuickConfig.DIRECTION_RES) {
			this.req(seq);
			switch (type) {
			case CLOSE -> this.closeReq(seq, buffer, socketAddress);
			case CONNECT -> this.connectReq(seq, buffer, socketAddress);
			case NEGOTIATE -> this.negotiateReq(seq, buffer, socketAddress);
			case TRANSPORT -> this.transportReq(seq, buffer, socketAddress);
			}
		} else if(dir == QuickConfig.DIRECTION_ACK) {
			switch (type) {
			case CLOSE -> this.closeAck(buffer);
			case CONNECT -> this.connectAck(buffer);
			case NEGOTIATE -> this.negotiateAck(buffer);
			case TRANSPORT -> this.transportAck(buffer);
			}
			this.ack(seq);
		} else {
			LOGGER.warn("未知快传方向：{}", dir);
		}
	}
	
	/**
	 * 处理请求
	 * 
	 * @param seq 请求编号
	 * 
	 * @throws NetException 网络异常
	 */
	private void req(int seq) throws NetException {
	}
	
	/**
	 * 处理响应
	 * 
	 * @param seq 响应编号
	 * 
	 * @throws NetException 网络异常
	 */
	private void ack(int seq) throws NetException {
		final long timestamp = System.currentTimeMillis();
		final long rtt = timestamp - this.timestamp;
		this.timestamp = timestamp;
		try {
			this.lock.lock();
			// 处理已经响应数据
			Entry<Integer, ByteBuffer> entry;
			final var iterator = this.sendMap.entrySet().iterator();
			while(iterator.hasNext()) {
				entry = iterator.next();
				// 编号是否已经处理
				final int diff = seq - entry.getKey();
				if(diff >= 0) {
					// 删除已经响应数据
					iterator.remove();
				} else {
					break;
				}
			}
			final int diff = this.sendSeq - seq;
			if(diff == 0) {
				// 如果完成唤醒等待线程
				if(
					this.sendSize >= this.fileSize &&
					this.sendMap.isEmpty()
				) {
					this.finishCondition.signalAll();
				} else if(rtt < 100) {
					this.semaphore.release(2);
				} else {
					this.semaphore.release();
				}
			} else if(diff < 2) {
				this.semaphore.release(2);
			} else if(diff < 8) {
				this.semaphore.release();
			} else {
				// 拥塞等差快速重传
				if(this.retrySeq == seq) {
					if(this.retryTimes++ >= QuickConfig.RETRY_TIMES) {
						this.retry(seq, QuickConfig.RETRY_PACK_SIZE_LOSS);
					}
				} else {
					this.retrySeq = seq;
					this.retryTimes = 0;
				}
			}
		} finally {
			this.lock.unlock();
		}
	}

	/**
	 * 重传
	 * 
	 * @param seq 重传序号
	 * @param size 重传数量
	 * 
	 * @throws NetException 网络异常
	 */
	private void retry(int seq, int size) throws NetException {
		if(this.sendMap.isEmpty()) {
			return;
		}
		LOGGER.debug("快传重传：{}-{}-{}", seq, size, this.semaphore.availablePermits());
		for (int index = 1; index <= size; index++) {
			final ByteBuffer retry = this.sendMap.get(seq + index);
			if(retry == null) {
				continue;
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
		final ByteBuffer buffer = ByteBuffer.allocate(QuickConfig.HEADER_LENGTH + bytes.length);
		buffer.put(QuickConfig.QUICK_HEADER);
		buffer.put(type.directionType(QuickConfig.DIRECTION_RES));
		buffer.putShort(this.sessionId);
		buffer.putInt(this.sendSeq);
		buffer.put(bytes);
		try {
			this.lock.lock();
			this.sendMap.put(this.sendSeq, buffer);
			this.sendSeq++;
		} finally {
			this.lock.unlock();
		}
		return buffer;
	}
	
	/**
	 * @param type 类型
	 * @param seq 编号
	 * @param bytes 数据
	 * 
	 * @return 响应
	 */
	public ByteBuffer newAck(QuickConfig.Type type, int seq, byte[] bytes) {
		final ByteBuffer buffer = ByteBuffer.allocate(QuickConfig.HEADER_LENGTH + bytes.length);
		buffer.put(QuickConfig.QUICK_HEADER);
		buffer.put(type.directionType(QuickConfig.DIRECTION_ACK));
		buffer.putShort(this.sessionId);
		buffer.putInt(seq);
		buffer.put(bytes);
		return buffer;
	}
	
	/**
	 * 连接
	 * 
	 * 0               8               16              24              32
	 * +---------------+---------------+---------------+---------------+
	 * | candidate length                                              |
	 * +---------------+---------------+---------------+---------------+
	 * | candidate ...
	 * +---------------+---------------+---------------+---------------+
	 * 
	 * @param candidate 候选
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
		try {
			this.lock.lock();
			this.connectCondition.await(SystemConfig.CONNECT_TIMEOUT_MILLIS, TimeUnit.MILLISECONDS);
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
			LOGGER.debug("快传等待连接异常", e);
		} finally {
			this.lock.unlock();
		}
		return this.connect;
	}
	
	/**
	 * 连接请求
	 * 
	 * @param seq 编号
	 * @param buffer 数据
	 * @param socketAddress 地址
	 * 
	 * @throws NetException 网络异常
	 */
	private void connectReq(int seq, ByteBuffer buffer, InetSocketAddress socketAddress) throws NetException {
		this.send(this.newAck(Type.CONNECT, seq, ByteUtils.remainingToBytes(buffer)), socketAddress);
	}
	
	/**
	 * 连接响应
	 * 
	 * @param buffer 数据
	 */
	private void connectAck(ByteBuffer buffer) {
		this.connect = true;
		this.candidate = Candidate.of(ByteUtils.intToString(buffer));
		this.candidateSocketAddress = NetUtils.buildSocketAddress(this.candidate.getHost(), this.candidate.getPort());
		LOGGER.debug("快传候选地址：{}", this.candidate);
		try {
			this.lock.lock();
			this.connectCondition.signalAll();
		} finally {
			this.lock.unlock();
		}
	}
	
	/**
	 * 协商
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
		try {
			this.lock.lock();
			this.negotiateCondition.await(SystemConfig.CONNECT_TIMEOUT_MILLIS, TimeUnit.MILLISECONDS);
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
			LOGGER.debug("快传等待协商异常", e);
		} finally {
			this.lock.unlock();
		}
	}
	
	/**
	 * 协商请求
	 * 
	 * @param seq 编号
	 * @param buffer 数据
	 * @param socketAddress 地址
	 * 
	 * @throws NetException 网络异常
	 */
	private void negotiateReq(int seq, ByteBuffer buffer, InetSocketAddress socketAddress) throws NetException {
		final long fileSize = buffer.getLong();
		final long recvSize = buffer.getLong();
		final String fileName = ByteUtils.intToString(buffer);
		try {
			this.lock.lock();
			this.recvFile = new File(DownloadConfig.getPath(fileName));
			this.fileSize = fileSize;
			if(this.recvFile.exists()) {
				this.recvSize = this.recvFile.length();
			} else {
				this.recvSize = recvSize;
			}
//			this.recvSeq = seq;
			this.recvSeq = seq + 1;
			final OutputStream outputStream;
			if(this.recvSize > 0L) {
				outputStream = new FileOutputStream(this.recvFile, true);
			} else {
				outputStream = new FileOutputStream(this.recvFile);
			}
			final BufferedOutputStream bufferedOutputStream = new BufferedOutputStream(outputStream, DownloadConfig.getMemoryBufferByte());
			this.output = Channels.newChannel(bufferedOutputStream);
		} catch (FileNotFoundException e) {
			throw new NetException("打开文件失败", e);
		} finally {
			this.lock.unlock();
		}
		this.send(this.newAck(Type.NEGOTIATE, seq, this.buildNegotiate(this.fileSize, this.recvSize, fileName)), socketAddress);
	}
	
	/**
	 * 协商响应
	 * 
	 * @param buffer 数据
	 * 
	 * @throws NetException 网络异常
	 */
	private void negotiateAck(ByteBuffer buffer) throws NetException {
		final long fileSize = buffer.getLong();
		final long recvSize = buffer.getLong();
		LOGGER.debug("快传文件协商：{}-{}", fileSize, recvSize);
		try {
			this.lock.lock();
			this.negotiate = true;
			if(recvSize == fileSize) {
				LOGGER.debug("快传完成：{}-{}-{}", this.semaphore.availablePermits(), this.recvFile, this.sendFile);
				this.finish = true;
			}
			this.sendSize = recvSize;
			final InputStream inputStream = new FileInputStream(this.sendFile);
			final BufferedInputStream bufferedInputStream = new BufferedInputStream(inputStream, DownloadConfig.getMemoryBufferByte());
			bufferedInputStream.skip(this.sendSize);
			this.input = Channels.newChannel(bufferedInputStream);
			this.negotiateCondition.signalAll();
		} catch (IOException e) {
			throw new NetException("文件定位错误：" + this.sendSize, e);
		} finally {
			this.lock.unlock();
		}
	}
	
	/**
	 * @param fileSize 文件大小
	 * @param recvSize 接收大小
	 * @param fileName 文件名称
	 * 
	 * @return 协商消息
	 */
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
	 * 文件快传
	 */
	private void quick() {
		try {
			// 获取信号量
			final boolean retry = !this.semaphore.tryAcquire(SystemConfig.RECEIVE_TIMEOUT, TimeUnit.SECONDS);
			if(this.finish) {
				return;
			}
			if(retry) {
				// 没有信号量重传
				this.retry(this.sendSeq - QuickConfig.RETRY_PACK_SIZE_TIMEOUT, QuickConfig.RETRY_PACK_SIZE_TIMEOUT);
			} else {
				if(this.sendSize < this.fileSize) {
					// 发送文件数据
					this.transport();
				} else if(!this.sendMap.isEmpty()) {
					// 等待响应完成
					this.retry(this.sendSeq - QuickConfig.RETRY_PACK_SIZE_TIMEOUT, QuickConfig.RETRY_PACK_SIZE_TIMEOUT);
					try {
						this.lock.lock();
						if(!this.sendMap.isEmpty()) {
							this.finishCondition.await(SystemConfig.RECEIVE_TIMEOUT_MILLIS, TimeUnit.MILLISECONDS);
						}
					} finally {
						this.lock.unlock();
					}
				} else {
					LOGGER.debug("快传完成：{}-{}-{}", this.semaphore.availablePermits(), this.recvFile, this.sendFile);
					this.finish = true;
				}
			}
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
			LOGGER.debug("获取信号量异常", e);
		} catch (NetException e) {
			LOGGER.error("发送文件异常", e);
		}
	}
	
	/**
	 * 传输
	 * 
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
			this.lock.lock();
			this.byteBuffer.putInt(0);
			final int size = this.input.read(this.byteBuffer);
			if(size <= 0) {
				LOGGER.debug("快传文件传输完成：{}", size);
				return;
			}
			this.byteBuffer.putInt(0, size);
			this.sendSize += size;
			this.byteBuffer.flip();
			bytes = ByteUtils.remainingToBytes(this.byteBuffer);
			this.byteBuffer.clear();
		} catch (IOException e) {
			throw new NetException("文件读取失败", e);
		} finally {
			this.lock.unlock();
		}
		this.send(this.newReq(Type.TRANSPORT, bytes), this.candidateSocketAddress);
	}
	
	/**
	 * 传输请求
	 * 
	 * @param seq 编号
	 * @param buffer 数据
	 * @param socketAddress 地址
	 * 
	 * @throws NetException 网络异常
	 */
	public void transportReq(int seq, ByteBuffer buffer, InetSocketAddress socketAddress) throws NetException {
		if(seq < this.recvSeq) {
			LOGGER.debug("已经过期数据：{}-{}", seq, this.recvSeq);
			return;
		}
		try {
			this.lock.lock();
			ByteBuffer local;
			int recvSeq = this.recvSeq;
			this.recvMap.put(seq, buffer);
			while(true) {
				local = this.recvMap.remove(recvSeq);
//				local = this.recvMap.remove(recvSeq + 1);
				if(local == null) {
					break;
				} else {
					recvSeq++;
				}
				final int size = local.getInt();
				this.recvSize += size;
				this.output.write(local);
			}
			this.recvSeq = recvSeq;
		} catch (IOException e) {
			throw new NetException("文件写入异常", e);
		} finally {
			this.lock.unlock();
		}
		if(this.recvSize >= this.fileSize) {
			LOGGER.debug("快传完成：{}-{}-{}", this.semaphore.availablePermits(), this.recvFile, this.sendFile);
			this.finish = true;
		}
		this.send(this.newAck(Type.TRANSPORT, this.recvSeq, new byte[0]), socketAddress);
	}
	
	/**
	 * 传输响应
	 * 
	 * @param buffer 数据
	 */
	public void transportAck(ByteBuffer buffer) {
		final double progress = 1.0D * this.sendSize / this.fileSize;
		if(progress >= 1.0D || progress - this.progress >= 1.0D) {
			this.progress = progress;
			this.consumer.accept(progress);
		}
	}
	
	/**
	 * 关闭请求
	 * 
	 * @param seq 编号
	 * @param buffer 数据
	 * @param socketAddress 地址
	 * 
	 * @throws NetException 网络异常
	 */
	public void closeReq(int seq, ByteBuffer buffer, InetSocketAddress socketAddress) throws NetException {
		this.send(this.newAck(Type.CLOSE, this.recvSeq, new byte[0]), socketAddress);
		// 不要调用this.close();导致循环
		super.close();
		this.release();
	}
	
	/**
	 * 关闭响应
	 * 
	 * @param buffer 数据
	 */
	public void closeAck(ByteBuffer buffer) {
	}
	
	@Override
	public void close() {
		try {
			this.send(this.newReq(Type.CLOSE, new byte[0]), this.candidateSocketAddress);
		} catch (NetException e) {
			LOGGER.error("关闭快传通道异常", e);
		}
		super.close();
		this.release();
	}
	
	/**
	 * 释放资源
	 */
	private void release() {
		this.sendSeq = 0;
		this.recvSeq = 0;
		this.timestamp = System.currentTimeMillis();
		this.fileSize = 0L;
		this.sendSize = 0L;
		this.recvSize = 0L;
		this.finish = false;
		this.connect = false;
		this.sendFile = null;
		this.recvFile = null;
		this.candidate = null;
		IoUtils.close(this.input);
		this.input = null;
		IoUtils.close(this.output);
		this.output = null;
		this.candidateSocketAddress = null;
		this.semaphore.drainPermits();
		this.semaphore.release(QuickConfig.DEFAULT_SEMAPHORE);
		this.byteBuffer.clear();
		this.sendMap.clear();
		this.recvMap.clear();
		this.context.remove(this.key);
	}
	
}
