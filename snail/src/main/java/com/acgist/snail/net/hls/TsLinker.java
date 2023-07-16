package com.acgist.snail.net.hls;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Collectors;

import javax.crypto.Cipher;

import com.acgist.snail.config.SystemConfig;
import com.acgist.snail.logger.Logger;
import com.acgist.snail.logger.LoggerFactory;
import com.acgist.snail.utils.FileUtils;

/**
 * TS文件连接器
 * 将下载的文件列表链接成为一个文件
 * 
 * @author acgist
 */
public final class TsLinker {

    private static final Logger LOGGER = LoggerFactory.getLogger(TsLinker.class);
    
    /**
     * 文件后缀
     */
    private static final String SUFFIX = ".ts";
    
    /**
     * 任务名称
     */
    private final String name;
    /**
     * 文件路径
     */
    private final String path;
    /**
     * 加密套件
     */
    private final Cipher cipher;
    /**
     * 文件链接列表
     */
    private final List<String> links;
    
    /**
     * @param name   任务名称
     * @param path   文件路径
     * @param cipher 加密套件
     * @param links  文件列表
     */
    private TsLinker(String name, String path, Cipher cipher, List<String> links) {
        this.name   = name;
        this.path   = path;
        this.cipher = cipher;
        this.links  = links;
    }
    
    /**
     * 新建TS文件连接器
     * 
     * @param name   任务名称
     * @param path   文件路径
     * @param cipher 加密套件
     * @param links  文件列表
     * 
     * @return TS文件连接器
     */
    public static final TsLinker newInstance(String name, String path, Cipher cipher, List<String> links) {
        return new TsLinker(name, path, cipher, links);
    }
    
    /**
     * 连接文件
     * 
     * @return 文件大小
     */
    public long link() {
        final List<File> files = this.links.stream()
            .map(link -> Paths.get(this.path, FileUtils.fileName(link)).toFile())
            .collect(Collectors.toList());
        final File target = Paths.get(this.path, this.name + SUFFIX).toFile();
        // TODO：NIO优化
        try(final OutputStream output = new FileOutputStream(target)) {
            for (File file : files) {
                LOGGER.debug("连接文件：{} - {}", file, target);
                this.link(file, output);
            }
        } catch (IOException e) {
            LOGGER.error("文件连接异常", e);
        }
        return FileUtils.fileSize(target.getAbsolutePath());
    }
    
    /**
     * 连接文件
     * 
     * @param file   文件
     * @param output 输出流
     * 
     * @throws IOException IO异常
     */
    private void link(File file, OutputStream output) throws IOException {
        int length = 0;
        final byte[] bytes  = new byte[SystemConfig.DEFAULT_EXCHANGE_LENGTH];
        final boolean crypt = this.cipher != null;
        try(final InputStream input = new FileInputStream(file)) {
            while((length = input.read(bytes)) >= 0) {
                if(crypt) {
                    final byte[] decrypt = this.cipher.update(bytes, 0, length);
                    output.write(decrypt, 0, decrypt.length);
                } else {
                    output.write(bytes, 0, length);
                }
            }
        } finally {
            FileUtils.delete(file);
        }
    }
    
}
