package com.acgist.snail.net.torrent;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import com.acgist.snail.config.SymbolConfig;
import com.acgist.snail.config.SystemConfig;
import com.acgist.snail.utils.CollectionUtils;
import com.acgist.snail.utils.MapUtils;
import com.acgist.snail.utils.StringUtils;

/**
 * 文件信息
 * 
 * @author acgist
 */
public final class TorrentFile extends TorrentFileMatedata implements Serializable {

    private static final long serialVersionUID = 1L;
    
    /**
     * 文件路径分隔符
     */
    public static final String SEPARATOR = SymbolConfig.Symbol.SLASH.toString();
    /**
     * 填充文件前缀：{@value}
     * 注意：不用显示（不能直接排除防止计算文件偏移错误）
     */
    public static final String PADDING_FILE_PREFIX = "_____padding_file";
    /**
     * 文件路径：{@value}
     */
    public static final String ATTR_PATH = "path";
    /**
     * 文件路径（UTF8）：{@value}
     */
    public static final String ATTR_PATH_UTF8 = "path.utf-8";
    
    /**
     * 路径
     */
    private List<String> path;
    /**
     * 路径（UTF8）
     */
    private List<String> pathUtf8;
    /**
     * 是否选择下载
     */
    private transient boolean selected = false;
    /**
     * 是否是填充文件
     */
    private transient boolean paddingFile = false;

    protected TorrentFile() {
    }
    
    /**
     * 读取文件信息
     * 
     * @param map      文件信息
     * @param encoding 编码
     * 
     * @return 文件信息
     */
    public static final TorrentFile valueOf(Map<?, ?> map, String encoding) {
        Objects.requireNonNull(map, "文件信息为空");
        final TorrentFile file = new TorrentFile();
        file.setEd2k(MapUtils.getBytes(map, ATTR_ED2K));
        file.setLength(MapUtils.getLong(map, ATTR_LENGTH));
        file.setFilehash(MapUtils.getBytes(map, ATTR_FILEHASH));
        final List<Object> path = MapUtils.getList(map, ATTR_PATH);
        final List<String> pathList = readPath(path, encoding);
        file.setPath(pathList);
        final List<Object> pathUtf8 = MapUtils.getList(map, ATTR_PATH_UTF8);
        final List<String> pathUtf8List = readPath(pathUtf8, SystemConfig.CHARSET_UTF8);
        file.setPathUtf8(pathUtf8List);
        file.paddingFile = readPaddingFile(pathList, pathUtf8List);
        return file;
    }

    /**
     * @return 是否选择下载
     */
    public boolean selected() {
        return this.selected;
    }

    /**
     * @param selected 是否选择下载
     */
    public void selected(boolean selected) {
        this.selected = selected;
    }
    
    /**
     * @return 文件路径
     */
    public String path() {
        if (CollectionUtils.isNotEmpty(this.pathUtf8)) {
            return String.join(TorrentFile.SEPARATOR, this.pathUtf8);
        }
        return String.join(TorrentFile.SEPARATOR, this.path);
    }
    
    /**
     * 判断是否是填充文件
     * 
     * @return 是否是填充文件
     */
    public boolean paddingFile() {
        return this.paddingFile;
    }
    
    /**
     * 判断是否不是填充文件
     * 
     * @return 是否不是填充文件
     */
    public boolean notPaddingFile() {
        return !this.paddingFile();
    }
    
    /**
     * 读取文件路径
     * 
     * @param path     文件路径信息
     * @param encoding 编码
     * 
     * @return 文件路径
     */
    private static final List<String> readPath(List<Object> path, String encoding) {
        if(path == null) {
            return new ArrayList<>();
        }
        return path.stream()
            .map(value -> StringUtils.getCharsetString(value, encoding))
            .collect(Collectors.toList());
    }

    /**
     * 判断文件是否是填充文件
     * 
     * @param pathList     路径
     * @param pathUtf8List 路径（UTF-8）
     * 
     * @return 是否是填充文件
     */
    private static final boolean readPaddingFile(List<String> pathList, List<String> pathUtf8List) {
        String fileName = null;
        if(CollectionUtils.isNotEmpty(pathUtf8List)) {
            fileName = pathUtf8List.get(pathUtf8List.size() - 1);
        } else if(CollectionUtils.isNotEmpty(pathList)) {
            fileName = pathList.get(pathList.size() - 1);
        }
        return fileName != null && fileName.startsWith(PADDING_FILE_PREFIX);
    }
    
    /**
     * @return 路径
     */
    public List<String> getPath() {
        return this.path;
    }

    /**
     * @param path 路径
     */
    public void setPath(List<String> path) {
        this.path = path;
    }

    /**
     * @return 路径（UTF8）
     */
    public List<String> getPathUtf8() {
        return this.pathUtf8;
    }

    /**
     * @param pathUtf8 路径（UTF8）
     */
    public void setPathUtf8(List<String> pathUtf8) {
        this.pathUtf8 = pathUtf8;
    }

    @Override
    public String toString() {
        // TODO：实现
        return super.toString();
    }
    
}