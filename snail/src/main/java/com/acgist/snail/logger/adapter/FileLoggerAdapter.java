package com.acgist.snail.logger.adapter;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.text.SimpleDateFormat;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;

import com.acgist.snail.logger.LoggerAdapter;
import com.acgist.snail.logger.LoggerConfig;
import com.acgist.snail.logger.LoggerFactory;

/**
 * <p>文件适配器</p>
 * 
 * @author acgist
 */
public final class FileLoggerAdapter extends LoggerAdapter {

	/**
	 * <p>文件适配器名称：{@value}</p>
	 */
	public static final String ADAPTER = "file";
	/**
	 * <p>文件格式：{@value}</p>
	 */
	private static final String FILE_SUFFIX_FORMAT = ".yyyy.MM.dd";
	
	public FileLoggerAdapter() {
		final OutputStream output = this.buildOutput();
		this.output = output;
		this.errorOutput = output;
	}

	@Override
	public void release() {
		super.release();
		final File file = this.buildFile();
		final String fileName = file.getName();
		final LocalDateTime time = LocalDateTime.now();
		final int maxDays = LoggerConfig.getFileMaxDays();
		final File[] childrens = file.getParentFile().listFiles();
		for (File children : childrens) {
			if(this.deleteable(maxDays, fileName, children, time)) {
				try {
					Files.delete(children.toPath());
				} catch (IOException e) {
					LoggerFactory.error(e);
				}
			}
		}
	}
	
	/**
	 * <p>新建文件输出流</p>
	 * 
	 * @return 文件输出流
	 */
	private OutputStream buildOutput() {
		final int fileBuffer = LoggerConfig.getFileBuffer();
		final File file = this.buildFile();
		try {
			return new BufferedOutputStream(new FileOutputStream(file, true), fileBuffer);
		} catch (IOException e) {
			LoggerFactory.error(e);
		}
		return null;
	}
	
	/**
	 * <p>新建日志文件</p>
	 * 
	 * @return 日志文件
	 */
	private File buildFile() {
		final String fileName = LoggerConfig.getFileName();
		final SimpleDateFormat format = new SimpleDateFormat(FILE_SUFFIX_FORMAT);
		final String path = fileName + format.format(new Date());
		final File file = new File(path);
		// 创建上级目录
		final File parent = file.getParentFile();
		if(!parent.exists()) {
			parent.mkdirs();
		}
		return file;
	}
	
	/**
	 * <p>判断文件是否能够删除</p>
	 * 
	 * @param maxDays 最大备份数量
	 * @param fileName 日志文件名称
	 * @param logFile 日志文件
	 * @param time 当前时间
	 * 
	 * @return 是否可以删除
	 */
	private boolean deleteable(int maxDays, String fileName, File logFile, LocalDateTime time) {
		if(!logFile.exists() || !logFile.isFile()) {
			return false;
		}
		// 判断修改时间
		final LocalDateTime modifyTime = LocalDateTime.ofInstant(Instant.ofEpochMilli(logFile.lastModified()), ZoneId.systemDefault());
		final long days = Duration.between(modifyTime, time).toDays();
		if(days <= maxDays) {
			return false;
		}
		// 判断文件名称
		final String logFileName = logFile.getName();
		if(logFileName.length() != fileName.length()) {
			return false;
		}
		// snail.log.yyyy.MM.dd
		final int length = logFileName.length() - FILE_SUFFIX_FORMAT.length();
		if(length <= 0) {
			return false;
		}
		return logFileName.substring(0, length).equals(fileName.substring(0, length));
	}
	
}
