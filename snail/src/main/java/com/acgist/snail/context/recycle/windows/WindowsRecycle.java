package com.acgist.snail.context.recycle.windows;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.acgist.snail.context.recycle.Recycle;
import com.acgist.snail.utils.DateUtils;
import com.acgist.snail.utils.FileUtils;
import com.acgist.snail.utils.NumberUtils;

/**
 * <p>Windows回收站</p>
 * <p>支持系统：Win10</p>
 * 
 * @author acgist
 */
public final class WindowsRecycle extends Recycle {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(WindowsRecycle.class);

	/**
	 * <p>回收站路径：{@value}</p>
	 */
	private static final String RECYCLE_FOLDER = "$RECYCLE.BIN";
	/**
	 * <p>删除文件前缀：{@value}</p>
	 */
	private static final String FILE_PREFIX = "$R";
	/**
	 * <p>删除信息文件前缀：{@value}</p>
	 */
	private static final String INFO_PREFIX = "$I";
	
	/**
	 * <p>回收站路径</p>
	 */
	private String recyclePath;
	/**
	 * <p>删除文件路径</p>
	 */
	private String deleteFilePath;
	/**
	 * <p>删除信息文件路径</p>
	 */
	private String deleteInfoFilePath;

	/**
	 * @param path 文件路径
	 */
	public WindowsRecycle(String path) {
		super(path);
		this.buildRecycle();
		this.buildRecycleInfo();
	}

	/**
	 * <p>设置回收站路径</p>
	 */
	private void buildRecycle() {
		// 获取盘符
		final String disk = this.path.substring(0, 1).toUpperCase();
		// 获取回收站上级目录
		final String recycleFolder = FileUtils.file(disk + ":", RECYCLE_FOLDER);
		final File recycleFile = new File(recycleFolder);
		if(!recycleFile.exists()) {
			throw new IllegalArgumentException("回收站上级目录不存在：" + recycleFolder);
		}
		// 获取当前用户回收站文件目录
		final File[] files = recycleFile.listFiles();
		for (File file : files) {
			// 其他用户回收站没有权限查看：获取不到文件列表
			if(file.listFiles() != null) {
				this.recyclePath = file.getAbsolutePath();
				LOGGER.debug("回收站路径：{}", this.recyclePath);
				break;
			}
		}
	}
	
	/**
	 * <p>设置删除信息：删除文件、删除信息文件</p>
	 */
	private void buildRecycleInfo() {
		String ext = null;
		String name = NumberUtils.build().toString();
		if(this.file.isFile()) {
			ext = FileUtils.fileExt(this.path);
		}
		if(ext != null) {
			name = name + "." + ext;
		}
		this.deleteFilePath = FileUtils.file(this.recyclePath, FILE_PREFIX + name);
		this.deleteInfoFilePath = FileUtils.file(this.recyclePath, INFO_PREFIX + name);
		// TODO：换行
		LOGGER.debug("删除文件路径：{}，删除信息文件路径：{}", this.deleteFilePath, this.deleteInfoFilePath);
	}
	
	/**
	 * <p>删除文件</p>
	 * <p>文件移动成为删除文件</p>
	 * 
	 * @return 是否删除成功
	 */
	private boolean buildFile() {
		return this.file.renameTo(new File(this.deleteFilePath));
	}

	/**
	 * <p>创建删除信息文件</p>
	 */
	private void buildDeleteInfoFile() {
		FileUtils.write(this.deleteInfoFilePath, this.buildDeleteInfo());
	}
	
	/**
	 * <p>创建删除信息</p>
	 * 
	 * @return 删除信息
	 */
	private byte[] buildDeleteInfo() {
		final String path = FileUtils.systemSeparator(this.path);
		final ByteArrayOutputStream out = new ByteArrayOutputStream();
		// 固定值
		out.write(2);
		// 固定值
		for (int index = 0; index < 15; index++) {
			out.write(0);
		}
		// 时间戳
		final long timestamp = DateUtils.windowsTimestamp();
		final ByteBuffer buffer = ByteBuffer.allocate(8);
		// 设置CPU默认大小端模式
		buffer.order(ByteOrder.nativeOrder());
		buffer.putLong(timestamp);
		for (byte value : buffer.array()) {
			out.write(value);
		}
		// 固定值 + 文件路径长度
		final char length = (char) (1 + path.length());
		this.buildInfoChar(out, length);
		// 固定值
		for (int index = 0; index < 2; index++) {
			out.write(0);
		}
		// 文件路径
		for (int index = 0; index < path.length(); index++) {
			this.buildInfoChar(out, path.charAt(index));
		}
		// 固定值
		for (int index = 0; index < 2; index++) {
			out.write(0);
		}
		return out.toByteArray();
	}
	
	/**
	 * <p>写入删除信息数据</p>
	 * <p>注意：CPU大小端</p>
	 * 
	 * @param out 字符流
	 * @param value 数据
	 */
	private void buildInfoChar(ByteArrayOutputStream out, char value) {
		if(value > 0xFF) {
			if(ByteOrder.nativeOrder() == ByteOrder.LITTLE_ENDIAN) {
				// 小端
				out.write(value & 0xFF);
				out.write(value >> 8 & 0xFF);
			} else {
				// 大端
				out.write(value >> 8 & 0xFF);
				out.write(value & 0xFF);
			}
		} else {
			if(ByteOrder.nativeOrder() == ByteOrder.LITTLE_ENDIAN) {
				// 小端
				out.write(value);
				out.write(0);
			} else {
				// 大端
				out.write(0);
				out.write(value);
			}
		}
	}
	
	@Override
	public boolean delete() {
		LOGGER.debug("删除文件：{}", this.path);
		if(this.buildFile()) {
			this.buildDeleteInfoFile();
			return true;
		}
		return false;
	}
	
}
