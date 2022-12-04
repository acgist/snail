package com.acgist.snail.downloader.ftp;

import java.nio.channels.Channels;

import com.acgist.snail.context.ITaskSession;
import com.acgist.snail.downloader.SingleFileDownloader;
import com.acgist.snail.net.NetException;
import com.acgist.snail.net.ftp.FtpClient;
import com.acgist.snail.utils.FileUtils;
import com.acgist.snail.utils.IoUtils;

/**
 * <p>FTP任务下载器</p>
 * 
 * @author acgist
 */
public final class FtpDownloader extends SingleFileDownloader {
	
	/**
	 * <p>FTP客户端</p>
	 */
	private FtpClient client;
	
	/**
	 * @param taskSession 任务信息
	 */
	private FtpDownloader(ITaskSession taskSession) {
		super(taskSession);
	}

	/**
	 * <p>新建FTP任务下载器</p>
	 * 
	 * @param taskSession 任务信息
	 * 
	 * @return {@link FtpDownloader}
	 */
	public static final FtpDownloader newInstance(ITaskSession taskSession) {
		return new FtpDownloader(taskSession);
	}

	@Override
	public void release() {
		if(this.client != null) {
			this.client.close();
		}
		IoUtils.close(this.input);
		IoUtils.close(this.output);
		super.release();
	}
	
	@Override
	protected void buildInput() throws NetException {
		this.client = FtpClient.newInstance(this.taskSession.getUrl());
		final boolean success = this.client.connect();
		if(success) {
			final long downloadSize = FileUtils.fileSize(this.taskSession.getFile());
			this.input = Channels.newChannel(this.client.download(downloadSize));
			if(this.client.range()) {
				this.taskSession.downloadSize(downloadSize);
			} else {
				this.taskSession.downloadSize(0L);
			}
		} else {
			this.fail("FTP服务器连接失败");
		}
	}
	
}
