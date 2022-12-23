package com.acgist.snail.net.quick;

import java.io.File;
import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Stream;

import com.acgist.snail.config.SymbolConfig;
import com.acgist.snail.net.NetException;
import com.acgist.snail.net.UdpClient;
import com.acgist.snail.net.torrent.TorrentServer;

/**
 * 快传客户端
 * 
 * TODO：端口转发？
 * 
 * @author acgist
 */
public class QuickClient extends UdpClient<QuickMessageHandler> {

	/**
	 * 是否连接
	 */
	private boolean connect;
	
	public QuickClient() {
		super("Quick Client", QuickContext.getInstance().build());
		this.connect = false;
	}

	@Override
	public boolean open() {
		return super.open(TorrentServer.getInstance().channel());
	}
	
	/**
	 * 开始连接
	 * 
	 * @param list 候选列表
	 * @param consumer 连接通知
	 * 
	 * @return 是否成功
	 * 
	 * @throws NetException 网络异常
	 */
	public boolean connect(String list, Consumer<Boolean> consumer) throws NetException {
		final List<Candidate> candidateList = this.list(list);
		this.connect = false;
		while(!this.connect && this.handler.available()) {
			for (Candidate candidate : candidateList) {
				this.connect = this.handler.connect(candidate);
				if(this.connect) {
					break;
				}
			}
		}
		consumer.accept(this.connect);
		return this.connect;
	}
	
	/**
	 * 传输文件
	 * 
	 * @param file 文件
	 * @param consumer 进度通知
	 * 
	 * @throws NetException 网络异常
	 */
	public void quick(File file, Consumer<Double> consumer) throws NetException {
		if(this.handler.available()) {
			this.handler.quick(file, consumer);
		}
	}
	
	@Override
	public void close() {
		super.close();
		this.connect = false;
	}
	
	/**
	 * @return 是否连接成功
	 */
	public boolean connect() {
		return this.connect;
	}
	
	/**
	 * @param list 候选
	 * 
	 * @return 候选列表
	 */
	private List<Candidate> list(String list) {
		final String[] array = SymbolConfig.Symbol.LINE_SEPARATOR.split(list);
		return Stream.of(array)
			.map(String::strip)
			.map(Candidate::of)
			.toList();
	}

}
