package com.acgist.snail.downloader.ftp;

import java.io.BufferedInputStream;

import com.acgist.snail.downloader.SingleFileDownloader;
import com.acgist.snail.net.ftp.FtpClient;
import com.acgist.snail.net.ftp.bootstrap.FtpClientBuilder;
import com.acgist.snail.pojo.ITaskSession;
import com.acgist.snail.system.exception.NetException;
import com.acgist.snail.utils.FileUtils;
import com.acgist.snail.utils.IoUtils;

/**
 * <p>FTP任务下载器</p>
 * 
 * @author acgist
 * @since 1.0.0
 */
public final class FtpDownloader extends SingleFileDownloader {
	
	/**
	 * <p>FTP客户端</p>
	 */
	private FtpClient client;
	
	private FtpDownloader(ITaskSession taskSession) {
		super(taskSession);
	}

	/**
	 * <p>创建FTP任务下载器</p>
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
			this.client.close(); // 关闭FTP客户端
		}
		IoUtils.close(this.input);
		IoUtils.close(this.output);
		super.release();
	}
	
	@Override
	protected void buildInput() throws NetException {
		// FTP客户端
		this.client = FtpClientBuilder.newInstance(this.taskSession.getUrl()).build();
		final boolean ok = this.client.connect(); // 建立连接
		if(ok) {
			// 已下载大小
			final long size = FileUtils.fileSize(this.taskSession.getFile());
			final var inputStream = this.client.download(size);
			this.input = new BufferedInputStream(inputStream);
			if(this.client.range()) { // 支持断点续传
				this.taskSession.downloadSize(size);
			} else {
				this.taskSession.downloadSize(0L);
			}
		} else {
			fail("FTP服务器连接失败");
		}
	}
	
}
