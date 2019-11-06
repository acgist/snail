package com.acgist.snail.protocol.ftp;

import com.acgist.snail.downloader.IDownloader;
import com.acgist.snail.downloader.ftp.FtpDownloader;
import com.acgist.snail.net.ftp.FtpClient;
import com.acgist.snail.net.ftp.bootstrap.FtpClientBuilder;
import com.acgist.snail.pojo.ITaskSession;
import com.acgist.snail.protocol.Protocol;
import com.acgist.snail.system.exception.DownloadException;
import com.acgist.snail.system.exception.NetException;

/**
 * FTP协议
 * 
 * @author acgist
 * @since 1.0.0
 */
public class FtpProtocol extends Protocol {
	
	private static final FtpProtocol INSTANCE = new FtpProtocol();
	
	private FtpProtocol() {
		super(Type.FTP);
	}
	
	public static final FtpProtocol getInstance() {
		return INSTANCE;
	}

	@Override
	public String name() {
		return "FTP";
	}
	
	@Override
	public boolean available() {
		return true;
	}
	
	@Override
	public IDownloader buildDownloader(ITaskSession taskSession) {
		return FtpDownloader.newInstance(taskSession);
	}

	@Override
	protected void buildSize() throws DownloadException {
		final FtpClient client = FtpClientBuilder.newInstance(this.url).build();
		try {
			client.connect();
			final long size = client.size();
			this.taskEntity.setSize(size);
		} catch (NetException e) {
			throw new DownloadException(e);
		} finally {
			client.close();
		}
	}

	@Override
	protected void cleanMessage(boolean ok) {
	}

}
