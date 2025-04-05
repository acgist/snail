package com.acgist.snail.protocol;

import java.io.File;

import com.acgist.snail.config.DownloadConfig;
import com.acgist.snail.config.SymbolConfig;
import com.acgist.snail.context.EntityContext;
import com.acgist.snail.context.ITaskSession;
import com.acgist.snail.context.ITaskSessionStatus.Status;
import com.acgist.snail.context.entity.TaskEntity;
import com.acgist.snail.context.session.TaskSession;
import com.acgist.snail.downloader.IDownloader;
import com.acgist.snail.net.DownloadException;
import com.acgist.snail.utils.FileUtils;
import com.acgist.snail.utils.StringUtils;

/**
 * 下载协议
 * 
 * @author acgist
 */
public abstract class Protocol implements IProtocol {
    
    /**
     * 磁力链接（标准）：{@value}
     */
    private static final String MAGNET_BASIC = "magnet:\\?.+";
    /**
     * 磁力链接（32位Hash）：{@value}
     */
    private static final String MAGNET_HASH_32 = "[a-zA-Z0-9]{32}";
    /**
     * 磁力链接（40位Hash）：{@value}
     */
    private static final String MAGNET_HASH_40 = "[a-zA-Z0-9]{40}";
    
    /**
     * 协议类型
     * 
     * @author acgist
     */
    public enum Type {

        /**
         * UDP
         */
        UDP(
            new String[] { "udp://.+" },
            new String[] { "udp://"   },
            new String[] {},
            "udp://",
            ""
        ),
        /**
         * TCP
         */
        TCP(
            new String[] { "tcp://.+" },
            new String[] { "tcp://"   },
            new String[] {},
            "tcp://",
            ""
        ),
        /**
         * FTP
         */
        FTP(
            new String[] { "ftp://.+" },
            new String[] { "ftp://"   },
            new String[] {},
            "ftp://",
            ""
        ),
        /**
         * HLS
         */
        HLS(
            new String[] {
                ".+\\.m3u8",
                "http://.+\\.m3u8",      "https://.+\\.m3u8",
                "http://.+\\.m3u8#.+",   "https://.+\\.m3u8#.+",
                "http://.+\\.m3u8\\?.+", "https://.+\\.m3u8\\?.+"
            },
            new String[] {},
            new String[] { ".m3u8" },
            "",
            ".m3u8"
        ),
        /**
         * HTTP、HTTPS
         */
        HTTP(
            new String[] { "http://.+", "https://.+" },
            new String[] { "http://",   "https://"   },
            new String[] {},
            "http://",
            ""
        ),
        /**
         * 磁力链接
         */
        MAGNET(
            new String[] { MAGNET_BASIC, MAGNET_HASH_32, MAGNET_HASH_40 },
            new String[] { "magnet:?xt=urn:btih:" },
            new String[] {},
            "magnet:?xt=urn:btih:",
            ""
        ),
        /**
         * 迅雷链接
         */
        THUNDER(
            new String[] { "thunder://.+" },
            new String[] { "thunder://"   },
            new String[] {},
            "thunder://",
            ""
        ),
        /**
         * BT：BitTorrent
         */
        TORRENT(
            new String[] { ".+\\.torrent" },
            new String[] {},
            new String[] { ".torrent" },
            "",
            ".torrent"
        );
        
        /**
         * 正则表达式
         */
        private final String[] regexs;
        /**
         * 前缀
         */
        private final String[] prefix;
        /**
         * 后缀
         */
        private final String[] suffix;
        /**
         * 默认前缀
         */
        private final String defaultPrefix;
        /**
         * 默认后缀
         */
        private final String defaultSuffix;
        
        /**
         * @param regexs        正则表达式
         * @param prefix        前缀
         * @param suffix        后缀
         * @param defaultPrefix 默认前缀
         * @param defaultSuffix 默认后缀
         */
        private Type(String[] regexs, String[] prefix, String[] suffix, String defaultPrefix, String defaultSuffix) {
            this.regexs = regexs;
            this.prefix = prefix;
            this.suffix = suffix;
            this.defaultPrefix = defaultPrefix;
            this.defaultSuffix = defaultSuffix;
        }

        /**
         * @return 正则表达式
         */
        public String[] regexs() {
            return this.regexs;
        }

        /**
         * @return 前缀
         */
        public String[] prefix() {
            return this.prefix;
        }
        
        /**
         * @param url 链接
         * 
         * @return 前缀
         */
        public String prefix(String url) {
            for (String value : this.prefix) {
                if(StringUtils.startsWith(url, value)) {
                    return value;
                }
            }
            return null;
        }
        
        /**
         * @return 后缀
         */
        public String[] suffix() {
            return this.suffix;
        }
        
        /**
         * @param url 链接
         * 
         * @return 后缀
         */
        public String suffix(String url) {
            for (String value : this.suffix) {
                if(StringUtils.endsWith(url, value)) {
                    return value;
                }
            }
            return null;
        }
        
        /**
         * @return 默认前缀
         */
        public String defaultPrefix() {
            return this.defaultPrefix;
        }
        
        /**
         * @return 默认后缀
         */
        public String defaultSuffix() {
            return this.defaultSuffix;
        }
        
        /**
         * 判断协议是否支持下载链接
         * 
         * @param url 链接
         * 
         * @return 是否支持下载链接
         */
        public boolean verify(String url) {
            for (String regex : this.regexs) {
                if(StringUtils.regex(url, regex, true)) {
                    return true;
                }
            }
            return false;
        }
        
        /**
         * 将Hash转为完整磁力链接
         * 
         * @param hash Hash
         * 
         * @return 完整磁力链接
         */
        public static final String buildMagnet(String hash) {
            if(Type.verifyMagnet(hash)) {
                return hash;
            }
            return Type.MAGNET.defaultPrefix + hash.toLowerCase();
        }
        
        /**
         * 判断是否是完整磁力链接
         * 
         * @param url 磁力链接
         * 
         * @return 是否是完整磁力链接
         */
        public static final boolean verifyMagnet(String url) {
            return StringUtils.regex(url, MAGNET_BASIC, true);
        }
        
        /**
         * 判断是否是32位磁力链接Hash
         * 
         * @param url 磁力链接
         * 
         * @return 是否是32位磁力链接Hash
         */
        public static final boolean verifyMagnetHash32(String url) {
            return StringUtils.regex(url, MAGNET_HASH_32, true);
        }
        
        /**
         * 判断是否是40位磁力链接Hash
         * 
         * @param url 磁力链接
         * 
         * @return 是否是40位磁力链接Hash
         */
        public static final boolean verifyMagnetHash40(String url) {
            return StringUtils.regex(url, MAGNET_HASH_40, true);
        }
        
    }
    
    /**
     * 协议类型
     */
    protected final Type type;
    /**
     * 协议名称
     */
    protected final String name;
    /**
     * 下载链接
     */
    protected String url;
    /**
     * 下载任务
     */
    protected TaskEntity taskEntity;
    
    /**
     * @param type 协议类型
     * @param name 协议名称
     */
    protected Protocol(Type type, String name) {
        this.type = type;
        this.name = name;
    }

    /**
     * @return 协议类型
     */
    public final Type type() {
        return this.type;
    }
    
    /**
     * @return 协议名称
     */
    public final String name() {
        return this.name;
    }
    
    /**
     * 判断协议是否支持下载链接
     * 
     * @param url 下载链接
     * 
     * @return 是否支持下载链接
     */
    public boolean verify(String url) {
        if(this.type == null) {
            return false;
        }
        return this.type.verify(url);
    }
    
    /**
     * 判断是否可用
     * 
     * @return 是否可用
     */
    public abstract boolean available();
    
    /**
     * 新建下载器
     * 
     * @param taskSession 下载任务
     * 
     * @return 下载器
     */
    public abstract IDownloader buildDownloader(ITaskSession taskSession);
    
    /**
     * 新建下载任务
     * 
     * @param url 下载链接
     * 
     * @return 任务信息
     * 
     * @throws DownloadException 下载异常
     */
    public synchronized ITaskSession buildTaskSession(String url) throws DownloadException {
        this.url = url.strip();
        boolean success = true;
        try {
            this.buildTaskEntity();
            return TaskSession.newInstance(this.taskEntity);
        } catch (DownloadException e) {
            success = false;
            throw e;
        } catch (Exception e) {
            success = false;
            throw new DownloadException("下载失败", e);
        } finally {
            if(success) {
                this.success();
            }
            this.release(success);
        }
    }

    /**
     * 新建下载任务
     * 
     * @throws DownloadException 下载异常
     */
    protected void buildTaskEntity() throws DownloadException {
        this.taskEntity = new TaskEntity();
        this.prep();
        this.buildUrl();
        this.buildType();
        this.buildStatus();
        final String fileName = this.buildFileName();
        this.buildName(fileName);
        this.buildFile(fileName);
        this.buildFileType(fileName);
        this.buildSize();
        this.done();
        this.persistentTaskEntity();
    }
    
    /**
     * 预处理
     * 
     * @throws DownloadException 下载异常
     */
    protected void prep() throws DownloadException {
    }
    
    /**
     * 设置URL
     * 
     * @throws DownloadException 下载异常
     */
    protected void buildUrl() throws DownloadException {
        this.taskEntity.setUrl(this.url);
    }

    /**
     * 设置下载类型
     * 
     * @throws DownloadException 下载异常
     */
    protected void buildType() throws DownloadException {
        this.taskEntity.setType(this.type);
    }

    /**
     * 设置任务状态
     * 
     * @throws DownloadException 下载异常
     */
    protected void buildStatus() throws DownloadException {
        this.taskEntity.setStatus(Status.AWAIT);
    }

    /**
     * @return 文件名称
     * 
     * @throws DownloadException 下载异常
     */
    protected String buildFileName() throws DownloadException {
        return FileUtils.fileName(this.url);
    }

    /**
     * 设置任务名称
     * 
     * @param fileName 文件名称
     * 
     * @throws DownloadException 下载异常
     */
    protected void buildName(String fileName) throws DownloadException {
        String taskName = fileName;
        // 去掉后缀
        final int index = fileName.lastIndexOf(SymbolConfig.Symbol.DOT.toChar());
        if(index != -1) {
            taskName = fileName.substring(0, index);
        }
        this.taskEntity.setName(taskName);
    }
    
    /**
     * 设置下载文件、目录
     * 
     * @param fileName 文件名称
     * 
     * @throws DownloadException 下载异常
     */
    protected void buildFile(String fileName) throws DownloadException {
        final String filePath = DownloadConfig.getPath(fileName);
        final File file = new File(filePath);
        if(file.exists()) {
            throw new DownloadException("下载文件已经存在：" + file);
        }
        this.taskEntity.setFile(filePath);
    }
    
    /**
     * 设置任务文件类型
     * 
     * @param fileName 文件名称
     * 
     * @throws DownloadException 下载异常
     */
    protected void buildFileType(String fileName) throws DownloadException {
        this.taskEntity.setFileType(FileUtils.fileType(fileName));
    }

    /**
     * 设置任务大小
     * 
     * @throws DownloadException 下载异常
     */
    protected void buildSize() throws DownloadException {
    }
    
    /**
     * 完成处理
     * 
     * @throws DownloadException 下载异常
     */
    protected void done() throws DownloadException {
    }
    
    /**
     * 持久化任务
     * 
     * @throws DownloadException 下载异常
     */
    protected void persistentTaskEntity() throws DownloadException {
        EntityContext.getInstance().save(this.taskEntity);
    }
    
    /**
     * 成功处理
     */
    protected void success() {
    }
    
    /**
     * 释放资源
     * 
     * @param success 是否成功
     */
    protected void release(boolean success) {
        this.url = null;
        this.taskEntity = null;
    }

}
