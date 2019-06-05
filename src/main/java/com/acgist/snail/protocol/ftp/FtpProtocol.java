package com.acgist.snail.protocol.ftp;

import com.acgist.snail.net.ftp.FtpClient;
import com.acgist.snail.net.ftp.bootstrap.FtpClientBuilder;
import com.acgist.snail.pojo.entity.TaskEntity;
import com.acgist.snail.pojo.entity.TaskEntity.Status;
import com.acgist.snail.pojo.entity.TaskEntity.Type;
import com.acgist.snail.protocol.Protocol;
import com.acgist.snail.system.exception.DownloadException;
import com.acgist.snail.system.exception.NetException;
import com.acgist.snail.utils.FileUtils;

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
	protected boolean buildTaskEntity() throws DownloadException {
		final TaskEntity taskEntity = new TaskEntity();
		final String fileName = buildFileName(); // 文件名称
		taskEntity.setUrl(this.url);
		taskEntity.setType(this.type);
		taskEntity.setStatus(Status.await);
		taskEntity.setName(buildName(fileName));
		taskEntity.setFile(buildFile(fileName));
		taskEntity.setFileType(FileUtils.fileType(fileName));
		taskEntity.setSize(buildSize());
		this.taskEntity = taskEntity;
		return true;
	}

	@Override
	protected void cleanMessage() {
	}

	private long buildSize() throws DownloadException {
		final FtpClient client = FtpClientBuilder.newInstance(this.url).build();
		long size = 0L;
		try {
			client.connect();
			size = client.size();
		} catch (NetException e) {
			throw new DownloadException(e.getMessage(), e);
		} finally {
			client.close();
		}
		return size;
	}
	
}
