package com.acgist.snail.logger.adapter;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;

import com.acgist.snail.logger.Logger;
import com.acgist.snail.logger.LoggerAdapter;
import com.acgist.snail.logger.LoggerConfig;

/**
 * <p>文件适配器</p>
 * 
 * @author acgist
 */
public final class FileLoggerAdapter extends LoggerAdapter {

	/**
	 * <p>适配器名称</p>
	 */
	public static final String ADAPTER = "file";
	
	public FileLoggerAdapter() {
		final OutputStream output = this.buildOutput();
		this.output = output;
		this.errorOutput = output;
	}

	/**
	 * <p>创建文件输出流</p>
	 * 
	 * @return 文件输出流
	 */
	private OutputStream buildOutput() {
		final int fileBuffer = LoggerConfig.getFileBuffer();
		final File file = this.buildFile();
		this.buildParent(file);
		try {
			return new BufferedOutputStream(new FileOutputStream(file, true), fileBuffer);
		} catch (IOException e) {
			Logger.error(e);
		}
		return null;
	}
	
	@Override
	public void release() {
		super.release();
		final File file = this.buildFile();
		final String fileName = file.getName();
		final LocalDateTime now = LocalDateTime.now();
		final int maxDays = LoggerConfig.getFileMaxDays();
		final File[] childrens = file.getParentFile().listFiles();
		for (File children : childrens) {
			if(this.deleteable(maxDays, fileName, now, children)) {
				children.delete();
			}
		}
	}
	
	/**
	 * <p>创建日志文件</p>
	 * 
	 * @return 日志文件
	 */
	private File buildFile() {
		final String fileName = LoggerConfig.getFileName();
		final SimpleDateFormat format = new SimpleDateFormat(".yyyy.MM.dd");
		final String path = fileName + format.format(new Date());
		return new File(path);
	}
	
	/**
	 * <p>创建上级目录</p>
	 * 
	 * @param file 日志文件
	 */
	private void buildParent(File file) {
		final File parent = file.getParentFile();
		if(!parent.exists()) {
			parent.mkdirs();
		}
	}
	
	/**
	 * <p>判断文件是否能够删除</p>
	 * <p>日志文件、超过最大备份数量</p>
	 * 
	 * @param maxDays 最大备份数量
	 * @param fileName 日志文件名称
	 * @param now 当前时间
	 * @param file 文件
	 * 
	 * @return 是否可以删除
	 */
	private boolean deleteable(int maxDays, String fileName, LocalDateTime now, File file) {
		// 判断修改时间
		final LocalDateTime localDateTime = LocalDateTime.ofInstant(Instant.ofEpochMilli(file.lastModified()), ZoneId.systemDefault());
		final long days = Duration.between(localDateTime, now).toDays();
		if(days <= maxDays) {
			return false;
		}
		// 判断文件名称
		final String name = file.getName();
		if(name.length() != fileName.length()) {
			return false;
		}
		// snail.log.yyyy.MM.dd
		if(name.length() <= 10) {
			return false;
		}
		return name.substring(0, 10).equals(fileName.substring(0, 10));
	}
	
}
