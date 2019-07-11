package com.acgist.snail.system.recycle.window;

import java.io.ByteArrayOutputStream;
import java.io.File;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.acgist.snail.system.exception.ArgumentException;
import com.acgist.snail.system.recycle.Recycle;
import com.acgist.snail.utils.DateUtils;
import com.acgist.snail.utils.FileUtils;
import com.acgist.snail.utils.UniqueCodeUtils;

/**
 * Window回收站
 * 
 * @author acgist
 * @since 1.1.0
 */
public class WindowRecycle extends Recycle {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(WindowRecycle.class);

	/**
	 * 回收站路径
	 */
	private static final String RECYCLE_FOLDER = "$RECYCLE.BIN";
	/**
	 * 删除文件前缀
	 */
	private static final String FILE_PREFIX = "$R";
	/**
	 * 删除文件信息文件前缀
	 */
	private static final String INFO_PREFIX = "$I";
	/**
	 * 回收站路径
	 */
	private String recycleFolder;
	/**
	 * 删除文件路径
	 */
	private String deleteFile;
	/**
	 * 删除文件信息文件路径
	 */
	private String deleteInfoFile;
	
	public WindowRecycle(String path) {
		super(path);
		this.buildRecycle();
		this.buildRecycleName();
	}

	/**
	 * 设置回收站路径
	 */
	private void buildRecycle() {
		String disk = this.path.substring(0, 1).toUpperCase();
		final String recycleFolder = disk + ":" + File.separator + RECYCLE_FOLDER;
		final File recycleFile = new File(recycleFolder);
		if(!recycleFile.exists()) {
			throw new ArgumentException("回收站文件不存在：" + recycleFolder);
		}
		// 获取当前用户回收站
		final File[] files = recycleFile.listFiles();
		for (File file : files) {
			if(file.listFiles() != null) {
				this.recycleFolder = file.getAbsolutePath();
				LOGGER.debug("回收站路径：{}", this.recycleFolder);
				break;
			}
		}
	}
	
	/**
	 * 设置回收文件名称
	 */
	private void buildRecycleName() {
		final int code = UniqueCodeUtils.build();
		String ext = null;
		String name = String.valueOf(code);
		if(this.file.isFile()) {
			ext = FileUtils.ext(this.path);
		}
		if(ext != null) {
			name = name + "." + ext;
		}
		this.deleteFile = this.recycleFolder + File.separator + FILE_PREFIX + name;
		this.deleteInfoFile = this.recycleFolder + File.separator + INFO_PREFIX + name;
		LOGGER.debug("删除文件路径：{}，删除信息文件路径：{}", this.deleteFile, this.deleteInfoFile);
	}
	
	/**
	 * 创建删除文件
	 */
	private boolean buildFile() {
		return this.file.renameTo(new File(this.deleteFile));
	}

	/**
	 * 创建删除文件信息文件
	 */
	private void buildInfoFile() {
		FileUtils.write(this.deleteInfoFile, buildInfo());
	}
	
	/**
	 * 创建删除文件信息：小端
	 */
	public byte[] buildInfo() {
		String path = buildPath();
		final ByteArrayOutputStream out = new ByteArrayOutputStream();
		out.write(2); // 固定值
		for (int index = 0; index < 15; index++) { // 固定值
			out.write(0);
		}
		final long timestamp = DateUtils.windowTimestamp();
//		final ByteBuffer buffer = ByteBuffer.allocate(8);
//		buffer.order(ByteOrder.LITTLE_ENDIAN);
//		buffer.putLong(time);
		out.write((byte) (timestamp & 0xFF));
		out.write((byte) (timestamp >> 8 & 0xFF));
		out.write((byte) (timestamp >> 16 & 0xFF));
		out.write((byte) (timestamp >> 24 & 0xFF));
		out.write((byte) (timestamp >> 32 & 0xFF));
		out.write((byte) (timestamp >> 40 & 0xFF));
		out.write((byte) (timestamp >> 48 & 0xFF));
		out.write((byte) (timestamp >> 56 & 0xFF));
		final char length = (char) (1 + path.length()); // 固定值 + path.length();
		if(length > 0xFF) {
			out.write(length & 0xFF);
			out.write(length >> 8 & 0xFF);
		} else {
			out.write(length);
			out.write(0);
		}
		for (int index = 0; index < 2; index++) { // 固定值
			out.write(0);
		}
		char value;
		for (int index = 0; index < path.length(); index++) {
			value = path.charAt(index);
			if(value > 0xFF) {
				out.write(value & 0xFF);
				out.write(value >> 8 & 0xFF);
			} else {
				out.write(value);
				out.write(0);
			}
		}
		for (int index = 0; index < 2; index++) { // 固定值
			out.write(0);
		}
		return out.toByteArray();
	}

	/**
	 * 删除文件信息需要将斜杠转换成系统斜杠。
	 */
	private String buildPath() {
		String path = this.path;
		if(path.contains("/")) {
			path = path.replace("/", File.separator);
		}
		return path;
	}
	
	@Override
	public boolean delete() {
		LOGGER.debug("删除文件：{}", this.path);
		if(this.buildFile()) {
			this.buildInfoFile();
			return true;
		}
		return false;
	}
	
}
