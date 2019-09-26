package com.acgist.snail.downloader.ftp;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;

import com.acgist.snail.downloader.SingleFileDownloader;
import com.acgist.snail.net.ftp.FtpClient;
import com.acgist.snail.net.ftp.bootstrap.FtpClientBuilder;
import com.acgist.snail.pojo.session.TaskSession;
import com.acgist.snail.utils.FileUtils;
import com.acgist.snail.utils.IoUtils;

/**
 * <p>FTP下载器</p>
 * 
 * @author acgist
 * @since 1.0.0
 */
public class FtpDownloader extends SingleFileDownloader {
	
//	private static final Logger LOGGER = LoggerFactory.getLogger(FtpDownloader.class);

	/**
	 * FtpClient
	 */
	private FtpClient client;
	
	private FtpDownloader(TaskSession taskSession) {
		super(new byte[128 * 1024], taskSession);
	}

	public static final FtpDownloader newInstance(TaskSession taskSession) {
		return new FtpDownloader(taskSession);
	}

	@Override
	public void open() {
		buildInput();
		buildOutput();
	}

	@Override
	public void download() throws IOException {
		int length = 0;
		while(ok()) {
			// TODO：阻塞线程，导致暂停不能正常结束。
			length = this.input.read(this.bytes, 0, this.bytes.length);
			if(isComplete(length)) {
				this.complete = true;
				break;
			}
			this.output.write(this.bytes, 0, length);
			this.download(length);
		}
	}

	@Override
	public void release() {
		if(this.client != null) {
			this.client.close();
		}
		IoUtils.close(this.output);
	}

	/**
	 * 任务是否完成：长度-1或者下载数据等于任务长度。
	 */
	private boolean isComplete(int length) {
		final long size = this.taskSession.entity().getSize();
		final long downloadSize = this.taskSession.downloadSize();
		return length == -1 || size == downloadSize;
	}
	
	/**
	 * 创建FTP输入流、设置已下载文件大小。
	 */
	private void buildInput() {
		final var entity = this.taskSession.entity();
		// 已下载大小
		final long size = FileUtils.fileSize(entity.getFile());
		// 创建FtpClient
		this.client = FtpClientBuilder.newInstance(entity.getUrl()).build();
		final boolean ok = this.client.connect();
		if(ok) {
			final InputStream inputStream = this.client.download(size);
			if(inputStream == null) {
				fail(this.client.failMessage());
			} else {
				this.input = new BufferedInputStream(inputStream);
				if(this.client.range()) {
					this.taskSession.downloadSize(size);
				} else {
					this.taskSession.downloadSize(0L);
				}
			}
		} else {
			fail("服务器连接失败");
		}
	}
	
}
