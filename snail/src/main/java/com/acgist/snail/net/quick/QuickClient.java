package com.acgist.snail.net.quick;

import java.io.File;
import java.util.List;
import java.util.stream.Stream;

import com.acgist.snail.config.SymbolConfig;
import com.acgist.snail.net.NetException;
import com.acgist.snail.net.UdpClient;
import com.acgist.snail.net.torrent.TorrentServer;

/**
 * 快传客户端
 * 
 * @author acgist
 */
public class QuickClient extends UdpClient<QuickMessageHandler> {

	/**
	 * 是否关闭
	 */
	private volatile boolean close;
	
	public QuickClient() {
		super("Quick Client", QuickMessageHandler.getInstance());
	}

	@Override
	public boolean open() {
		return super.open(TorrentServer.getInstance().channel());
	}
	
	public boolean quick(String list) throws NetException {
		final List<Candidate> array = this.list(list);
		boolean connect = false;
		while(!this.close && !connect) {
			for (Candidate candidate : array) {
				connect = this.handler.connect(candidate);
				if(connect) {
					break;
				}
			}
		}
		return connect;
	}
	
	public void quick(String list, File file) throws NetException {
		final boolean connect = this.quick(list);
		if(!this.close && connect) {
			this.handler.quick(file);
		}
	}
	
	@Override
	public void close() {
		super.close();
	}
	
	private List<Candidate> list(String list) {
		final String[] array = SymbolConfig.Symbol.LINE_SEPARATOR.split(list);
		return Stream.of(array)
			.map(String::strip)
			.map(Candidate::of)
			.toList();
	}

}
