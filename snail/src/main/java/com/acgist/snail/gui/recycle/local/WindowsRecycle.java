package com.acgist.snail.gui.recycle.local;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.file.Paths;

import com.acgist.snail.config.SymbolConfig;
import com.acgist.snail.gui.recycle.Recycle;
import com.acgist.snail.logger.Logger;
import com.acgist.snail.logger.LoggerFactory;
import com.acgist.snail.utils.DateUtils;
import com.acgist.snail.utils.FileUtils;
import com.acgist.snail.utils.NumberUtils;

/**
 * Windows回收站
 * 
 * @author acgist
 */
public final class WindowsRecycle extends Recycle {
    
    private static final Logger LOGGER = LoggerFactory.getLogger(WindowsRecycle.class);

    /**
     * 删除文件前缀
     */
    private static final String FILE_PREFIX = "$R";
    /**
     * 删除信息文件前缀
     */
    private static final String INFO_PREFIX = "$I";
    /**
     * 回收站路径
     */
    private static final String RECYCLE_FOLDER = "$RECYCLE.BIN";
    
    /**
     * 回收站路径
     */
    private String recyclePath;
    /**
     * 删除文件路径
     */
    private String deleteFilePath;
    /**
     * 删除信息文件路径
     */
    private String deleteInfoFilePath;

    /**
     * Windows回收站
     * 
     * @param path 文件路径
     */
    public WindowsRecycle(String path) {
        super(path);
        this.buildRecyclePath();
        this.buildRecycleInfo();
    }

    /**
     * 设置回收站路径
     */
    private void buildRecyclePath() {
        final String disk = this.disk(this.path);
        final String recycleFolder = FileUtils.file(disk, RECYCLE_FOLDER);
        final File recycleFile     = new File(recycleFolder);
        if(!recycleFile.exists()) {
            throw new IllegalArgumentException("回收站上级目录不存在：" + recycleFolder);
        }
        // 当前用户回收站文件目录
        final File[] files = recycleFile.listFiles();
        for (File file : files) {
            // 其他用户没有权限查看用户回收站
            if(file.listFiles() != null) {
                this.recyclePath = file.getAbsolutePath();
                LOGGER.debug("回收站路径：{}", this.recyclePath);
                break;
            }
        }
    }
    
    /**
     * @param filePath 文件路径
     * 
     * @return 盘符
     */
    private String disk(String filePath) {
        final int diskIndex = filePath.indexOf(SymbolConfig.Symbol.COLON.toChar());
        if(diskIndex < 0) {
            // 相对路径
            final String absolutePath = Paths.get(filePath).toFile().getAbsolutePath();
            return this.disk(absolutePath);
        }
        return filePath.substring(0, diskIndex + 1).toUpperCase();
    }
    
    /**
     * 设置删除信息
     */
    private void buildRecycleInfo() {
        String name = NumberUtils.build().toString();
        if(this.file.isFile()) {
            final String ext = FileUtils.fileExt(this.path);
            if(ext != null) {
                name = SymbolConfig.Symbol.DOT.join(name, ext);
            }
        }
        this.deleteFilePath     = FileUtils.file(this.recyclePath, FILE_PREFIX + name);
        this.deleteInfoFilePath = FileUtils.file(this.recyclePath, INFO_PREFIX + name);
        LOGGER.debug("""
            删除文件路径：{}
            删除信息文件路径：{}
            """,
            this.deleteFilePath,
            this.deleteInfoFilePath
        );
    }
    
    /**
     * 删除文件
     * 
     * @return 是否删除成功
     */
    private boolean deleteFile() {
        return this.file.renameTo(new File(this.deleteFilePath));
    }

    /**
     * 新建删除信息文件
     */
    private void buildDeleteInfoFile() {
        try {
            FileUtils.write(this.deleteInfoFilePath, this.buildDeleteInfo());
        } catch (IOException e) {
            LOGGER.error("新建删除信息文件异常", e);
        }
    }
    
    /**
     * 新建删除信息
     * 
     * @return 删除信息
     * 
     * @throws IOException IO异常
     */
    private byte[] buildDeleteInfo() throws IOException {
        final ByteBuffer buffer = ByteBuffer.allocate(Long.BYTES);
        final String path       = FileUtils.systemSeparator(this.path);
        final ByteArrayOutputStream out = new ByteArrayOutputStream();
        // 设置大小端：默认CPU
        buffer.order(ByteOrder.nativeOrder());
        // 固定值
        out.write(2);
        // 固定值
        out.write(new byte[7]);
        // 设置文件大小
        buffer.putLong(FileUtils.fileSize(this.deleteFilePath));
        out.write(buffer.array());
        buffer.clear();
        // 设置删除时间戳
        buffer.putLong(DateUtils.windowsTimestamp());
        out.write(buffer.array());
        buffer.clear();
        // 固定值 + 文件路径长度
        final char length = (char) (1 + path.length());
        this.buildInfoChar(out, length);
        // 固定值
        out.write(new byte[2]);
        // 设置文件路径
        for (int index = 0; index < path.length(); index++) {
            this.buildInfoChar(out, path.charAt(index));
        }
        // 固定值
        out.write(new byte[2]);
        return out.toByteArray();
    }
    
    /**
     * 写入删除信息数据
     * 
     * @param out 字符流
     * @param value 数据
     */
    private void buildInfoChar(ByteArrayOutputStream out, char value) {
//      Character.reverseBytes(value);
        if(ByteOrder.nativeOrder() == ByteOrder.LITTLE_ENDIAN) {
            // 小端
            out.write(value      & 0xFF);
            out.write(value >> 8 & 0xFF);
        } else {
            // 大端
            out.write(value >> 8 & 0xFF);
            out.write(value      & 0xFF);
        }
    }
    
    @Override
    public boolean delete() {
        LOGGER.info("回收文件：{} - {}", this.path, this.deleteFilePath);
        if(this.deleteFile()) {
            this.buildDeleteInfoFile();
            return true;
        }
        return false;
    }
    
}
