package com.acgist.snail.protocol.ftp;

import com.acgist.snail.downloader.IDownloader;
import com.acgist.snail.downloader.ftp.FtpDownloader;
import com.acgist.snail.net.ftp.FtpClient;
import com.acgist.snail.net.ftp.bootstrap.FtpClientBuilder;
import com.acgist.snail.pojo.entity.TaskEntity.Type;
import com.acgist.snail.pojo.session.TaskSession;
import com.acgist.snail.protocol.Protocol;
import com.acgist.snail.system.exception.DownloadException;

/**
 * FTP协议
 * 
 * @author acgist
 * @since 1.0.0
 */
public class FtpProtocol extends Protocol {
	
	public static final String FTP_REGEX = "ftp://.+";
	
	private static final FtpProtocol INSTANCE = new FtpProtocol();
	
	private FtpProtocol() {
		super(Type.ftp, FTP_REGEX);
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
	public IDownloader buildDownloader(TaskSession taskSession) {
		return FtpDownloader.newInstance(taskSession);
	}

	@Override
	protected void buildSize() throws DownloadException {
		long size = 0L;
		final FtpClient client = FtpClientBuilder.newInstance(this.url).build();
		try {
			client.connect();
			size = client.size();
		} catch (Exception e) {
			throw new DownloadException(e);
		} finally {
			client.close();
		}
		this.taskEntity.setSize(size);
	}

	@Override
	protected void cleanMessage(boolean ok) {
	}

}
