package com.acgist.snail.system.recycle.window;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.acgist.snail.system.exception.ArgumentException;
import com.acgist.snail.system.recycle.Recycle;
import com.acgist.snail.utils.DateUtils;
import com.acgist.snail.utils.FileUtils;
import com.acgist.snail.utils.NumberUtils;

/**
 * <p>Window回收站</p>
 * <p>支持系统：Win10</p>
 * 
 * @author acgist
 * @since 1.1.0
 */
public final class WindowRecycle extends Recycle {
	
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
	private String recyclePath;
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
	 * <p>是否支持系统</p>
	 * 
	 * @param osName 系统名称
	 * 
	 * @return true-支持；false-不支持；
	 */
	public static final boolean support(String osName) {
		return osName != null &&
			(
				"Windows 10".equals(osName)
			);
	}
	
	/**
	 * <p>设置回收站路径</p>
	 */
	private void buildRecycle() {
		final String disk = this.path.substring(0, 1).toUpperCase(); // 盘符
		final String recycleFolder = disk + ":" + File.separator + RECYCLE_FOLDER;
		final File recycleFile = new File(recycleFolder);
		if(!recycleFile.exists()) {
			throw new ArgumentException("回收站文件不存在：" + recycleFolder);
		}
		// 获取当前用户回收站：其他用户回收站没有权限查看，所以获取不到文件列表。
		final File[] files = recycleFile.listFiles();
		for (File file : files) {
			if(file.listFiles() != null) {
				this.recyclePath = file.getAbsolutePath();
				LOGGER.debug("回收站路径：{}", this.recyclePath);
				break;
			}
		}
	}
	
	/**
	 * <p>设置回收文件名称</p>
	 */
	private void buildRecycleName() {
		String ext = null;
		String name = NumberUtils.build().toString();
		if(this.file.isFile()) {
			ext = FileUtils.fileExt(this.path);
		}
		if(ext != null) {
			name = name + "." + ext;
		}
		this.deleteFile = this.recyclePath + File.separator + FILE_PREFIX + name;
		this.deleteInfoFile = this.recyclePath + File.separator + INFO_PREFIX + name;
		LOGGER.debug("删除文件路径：{}，删除信息文件路径：{}", this.deleteFile, this.deleteInfoFile);
	}
	
	/**
	 * <p>创建删除文件</p>
	 */
	private boolean buildFile() {
		return this.file.renameTo(new File(this.deleteFile));
	}

	/**
	 * <p>创建删除文件信息文件</p>
	 */
	private void buildInfoFile() {
		FileUtils.write(this.deleteInfoFile, buildInfo());
	}
	
	/**
	 * <p>创建删除文件信息</p>
	 */
	public byte[] buildInfo() {
		final String path = buildPath();
		final ByteArrayOutputStream out = new ByteArrayOutputStream();
		// 固定值
		out.write(2);
		// 固定值
		for (int index = 0; index < 15; index++) {
			out.write(0);
		}
		// 时间戳
		final long timestamp = DateUtils.windowTimestamp();
		final ByteBuffer buffer = ByteBuffer.allocate(8);
		// 设置CPU默认大小端模式
		buffer.order(ByteOrder.nativeOrder());
		buffer.putLong(timestamp);
		for (byte value : buffer.array()) {
			out.write(value);
		}
		// 固定值 + path.length();
		final char length = (char) (1 + path.length());
		if(length > 0xFF) {
			out.write(length & 0xFF);
			out.write(length >> 8 & 0xFF);
		} else {
			out.write(length);
			out.write(0);
		}
		// 固定值
		for (int index = 0; index < 2; index++) {
			out.write(0);
		}
		// 文件路径
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
		// 固定值
		for (int index = 0; index < 2; index++) {
			out.write(0);
		}
		return out.toByteArray();
	}

	/**
	 * <p>删除文件信息需要将斜杠转换成系统斜杠</p>
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
