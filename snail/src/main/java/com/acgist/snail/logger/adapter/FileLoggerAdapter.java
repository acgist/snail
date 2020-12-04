package com.acgist.snail.logger.adapter;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
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
		final String fileName = LoggerConfig.getFileName();
		final int fileBuffer = LoggerConfig.getFileBuffer();
		final SimpleDateFormat format = new SimpleDateFormat(".yyyy.MM.dd");
		final String path = fileName + format.format(new Date());
		try {
			final File file = new File(path);
			// 创建上级目录
			final File parent = file.getParentFile();
			if(!parent.exists()) {
				parent.mkdirs();
			}
			return new BufferedOutputStream(new FileOutputStream(file, true), fileBuffer);
		} catch (IOException e) {
			Logger.error(e);
		}
		return null;
	}

}
