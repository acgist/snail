package com.acgist.snail.downloader.ftp;

import java.io.BufferedInputStream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.acgist.snail.downloader.SingleFileDownloader;
import com.acgist.snail.net.ftp.FtpClient;
import com.acgist.snail.net.ftp.bootstrap.FtpClientBuilder;
import com.acgist.snail.pojo.ITaskSession;
import com.acgist.snail.system.exception.NetException;
import com.acgist.snail.utils.FileUtils;
import com.acgist.snail.utils.IoUtils;

/**
 * <p>FTP下载器</p>
 * 
 * @author acgist
 * @since 1.0.0
 */
public final class FtpDownloader extends SingleFileDownloader {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(FtpDownloader.class);

	/**
	 * <p>FTP客户端</p>
	 */
	private FtpClient client;
	
	private FtpDownloader(ITaskSession taskSession) {
		super(taskSession);
	}

	public static final FtpDownloader newInstance(ITaskSession taskSession) {
		return new FtpDownloader(taskSession);
	}

	@Override
	public void release() {
		if(this.client != null) {
			this.client.close();
		}
//		IoUtils.close(this.input); // FtpClient关闭
		IoUtils.close(this.output);
		super.release();
	}

	@Override
	protected void buildInput() {
		// 获取已下载大小
		final long size = FileUtils.fileSize(this.taskSession.getFile());
		// 创建FTP客户端
		this.client = FtpClientBuilder.newInstance(this.taskSession.getUrl()).build();
		final boolean ok = this.client.connect();
		if(ok) {
			try {
				final var inputStream = this.client.download(size);
				this.input = new BufferedInputStream(inputStream);
				if(this.client.range()) {
					this.taskSession.downloadSize(size);
				} else {
					this.taskSession.downloadSize(0L);
				}
			} catch (NetException e) {
				LOGGER.error("FTP下载异常", e);
				fail("FTP下载失败：" + e.getMessage());
			}
		} else {
			fail("FTP服务器连接失败");
		}
	}
	
}
