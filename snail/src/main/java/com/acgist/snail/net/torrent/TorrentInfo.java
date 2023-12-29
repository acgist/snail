package com.acgist.snail.net.torrent;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import com.acgist.snail.config.SystemConfig;
import com.acgist.snail.utils.MapUtils;

/**
 * 文件信息
 * 
 * @author acgist
 */
public final class TorrentInfo extends TorrentFileMatedata implements Serializable {

    private static final long serialVersionUID = 1L;
    
    /**
     * 私有种子：{@value}
     */
    public static final byte PRIVATE_TORRENT = 1;
    /**
     * 文件名称：{@value}
     */
    public static final String ATTR_NAME = "name";
    /**
     * 文件名称（UTF8）：{@value}
     */
    public static final String ATTR_NAME_UTF8 = "name.utf-8";
    /**
     * 特征信息：{@value}
     */
    public static final String ATTR_PIECES = "pieces";
    /**
     * Piece大小：{@value}
     */
    public static final String ATTR_PIECE_LENGTH = "piece length";
    /**
     * 发布者：{@value}
     */
    public static final String ATTR_PUBLISHER = "publisher";
    /**
     * 发布者（UTF8）：{@value}
     */
    public static final String ATTR_PUBLISHER_UTF8 = "publisher.utf-8";
    /**
     * 发布者URL：{@value}
     */
    public static final String ATTR_PUBLISHER_URL = "publisher-url";
    /**
     * 发布者URL（UTF8）：{@value}
     */
    public static final String ATTR_PUBLISHER_URL_UTF8 = "publisher-url.utf-8";
    /**
     * 私有种子：{@value}
     */
    public static final String ATTR_PRIVATE = "private";
    /**
     * 文件列表：{@value}
     */
    public static final String ATTR_FILES = "files";
    
    /**
     * 名称
     * 单文件种子使用
     */
    private String name;
    /**
     * 名称（UTF8）
     * 单文件种子使用
     */
    private String nameUtf8;
    /**
     * 特征信息
     * 所有PieceHash集合
     */
    private byte[] pieces;
    /**
     * Piece大小
     */
    private Long pieceLength;
    /**
     * 发布者
     */
    private String publisher;
    /**
     * 发布者（UTF8）
     */
    private String publisherUtf8;
    /**
     * 发布者URL
     */
    private String publisherUrl;
    /**
     * 发布者URL（UTF8）
     */
    private String publisherUrlUtf8;
    /**
     * 私有种子
     * 
     * @see #PRIVATE_TORRENT
     */
    private Long privateTorrent;
    /**
     * 文件列表
     * 多文件种子使用（单文件种子为空）
     */
    private List<TorrentFile> files;

    protected TorrentInfo() {
    }

    /**
     * 读取文件信息
     * 
     * @param map      文件信息
     * @param encoding 编码
     * 
     * @return 文件信息
     */
    public static final TorrentInfo valueOf(Map<String, Object> map, String encoding) {
        Objects.requireNonNull(map, "文件信息为空");
        final TorrentInfo info = new TorrentInfo();
        info.setName(MapUtils.getString(map, ATTR_NAME, encoding));
        info.setNameUtf8(MapUtils.getString(map, ATTR_NAME_UTF8));
        info.setEd2k(MapUtils.getBytes(map, ATTR_ED2K));
        info.setLength(MapUtils.getLong(map, ATTR_LENGTH));
        info.setFilehash(MapUtils.getBytes(map, ATTR_FILEHASH));
        info.setPieces(MapUtils.getBytes(map, ATTR_PIECES));
        info.setPieceLength(MapUtils.getLong(map, ATTR_PIECE_LENGTH));
        info.setPublisher(MapUtils.getString(map, ATTR_PUBLISHER, encoding));
        info.setPublisherUtf8(MapUtils.getString(map, ATTR_PUBLISHER_UTF8));
        info.setPublisherUrl(MapUtils.getString(map, ATTR_PUBLISHER_URL, encoding));
        info.setPublisherUrlUtf8(MapUtils.getString(map, ATTR_PUBLISHER_URL_UTF8));
        info.setPrivateTorrent(MapUtils.getLong(map, ATTR_PRIVATE));
        info.setFiles(readFiles(MapUtils.getList(map, ATTR_FILES), encoding));
        return info;
    }
    
    /**
     * 获取Piece数量
     * 
     * @return Piece数量
     */
    public int pieceSize() {
        return this.pieces.length / SystemConfig.SHA1_HASH_LENGTH;
    }
    
    /**
     * 判断是否是私有种子
     * 
     * @return 是否是私有种子
     */
    public boolean privateTorrent() {
        return this.privateTorrent != null && this.privateTorrent.byteValue() == PRIVATE_TORRENT;
    }
    
    /**
     * 获取下载文件列表（兼容单文件种子）
     * 注意：不能直接排除填充文件（需要计算文件偏移）
     * 
     * @return 下载文件列表
     */
    public List<TorrentFile> files() {
        if (this.files.isEmpty()) {
            // 单文件种子
            final TorrentFile file = new TorrentFile();
            file.setEd2k(this.ed2k);
            file.setLength(this.length);
            file.setFilehash(this.filehash);
            if (this.name != null) {
                file.setPath(List.of(this.name));
            } else {
                file.setPath(List.of());
            }
            if (this.nameUtf8 != null) {
                file.setPathUtf8(List.of(this.nameUtf8));
            } else {
                file.setPathUtf8(List.of());
            }
            return List.of(file);
        } else {
            // 多文件种子
            return this.files;
        }
    }
    
    /**
     * 读取多文件种子文件列表
     * 
     * @param files    文件信息
     * @param encoding 编码
     * 
     * @return 文件列表
     */
    private static final List<TorrentFile> readFiles(List<Object> files, String encoding) {
        if(files == null) {
            return new ArrayList<>();
        }
        return files.stream()
            .map(value -> (Map<?, ?>) value)
            .map(value -> TorrentFile.valueOf(value, encoding))
            .collect(Collectors.toList());
    }
    
    /**
     * @return 名称
     */
    public String getName() {
        return this.name;
    }

    /**
     * @param name 名称
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * @return 名称（UTF8）
     */
    public String getNameUtf8() {
        return this.nameUtf8;
    }

    /**
     * @param nameUtf8 名称（UTF8）
     */
    public void setNameUtf8(String nameUtf8) {
        this.nameUtf8 = nameUtf8;
    }

    /**
     * @return 特征信息
     */
    public byte[] getPieces() {
        return this.pieces;
    }

    /**
     * @param pieces 特征信息
     */
    public void setPieces(byte[] pieces) {
        this.pieces = pieces;
    }
    
    /**
     * @return Piece大小
     */
    public Long getPieceLength() {
        return this.pieceLength;
    }

    /**
     * @param pieceLength Piece大小
     */
    public void setPieceLength(Long pieceLength) {
        this.pieceLength = pieceLength;
    }

    /**
     * @return 发布者
     */
    public String getPublisher() {
        return this.publisher;
    }

    /**
     * @param publisher 发布者
     */
    public void setPublisher(String publisher) {
        this.publisher = publisher;
    }

    /**
     * @return 发布者（UTF8）
     */
    public String getPublisherUtf8() {
        return this.publisherUtf8;
    }

    /**
     * @param publisherUtf8 发布者（UTF8）
     */
    public void setPublisherUtf8(String publisherUtf8) {
        this.publisherUtf8 = publisherUtf8;
    }

    /**
     * @return 发布者URL
     */
    public String getPublisherUrl() {
        return this.publisherUrl;
    }

    /**
     * @param publisherUrl 发布者URL
     */
    public void setPublisherUrl(String publisherUrl) {
        this.publisherUrl = publisherUrl;
    }

    /**
     * @return 发布者URL（UTF8）
     */
    public String getPublisherUrlUtf8() {
        return this.publisherUrlUtf8;
    }

    /**
     * @param publisherUrlUtf8 发布者URL（UTF8）
     */
    public void setPublisherUrlUtf8(String publisherUrlUtf8) {
        this.publisherUrlUtf8 = publisherUrlUtf8;
    }

    /**
     * @return 私有种子
     */
    public Long getPrivateTorrent() {
        return this.privateTorrent;
    }

    /**
     * @param privateTorrent 私有种子
     */
    public void setPrivateTorrent(Long privateTorrent) {
        this.privateTorrent = privateTorrent;
    }

    /**
     * @return 文件列表
     */
    public List<TorrentFile> getFiles() {
        return this.files;
    }

    /**
     * @param files 文件列表
     */
    public void setFiles(List<TorrentFile> files) {
        this.files = files;
    }
    
    @Override
    public String toString() {
        // TODO：实现
        return super.toString();
    }

}