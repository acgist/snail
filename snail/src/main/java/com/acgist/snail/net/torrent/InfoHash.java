package com.acgist.snail.net.torrent;

import java.io.Serializable;

import com.acgist.snail.net.DownloadException;
import com.acgist.snail.protocol.Protocol;
import com.acgist.snail.utils.Base32Utils;
import com.acgist.snail.utils.BeanUtils;
import com.acgist.snail.utils.DigestUtils;
import com.acgist.snail.utils.PeerUtils;
import com.acgist.snail.utils.StringUtils;

/**
 * 种子InfoHash
 * 种子文件：包含所有信息
 * 磁力链接：size=0、info=null
 * 
 * @author acgist
 */
public final class InfoHash implements Serializable {

    private static final long serialVersionUID = 1L;
    
    /**
     * 种子info数据长度
     */
    private int size;
    /**
     * 种子info数据
     */
    private byte[] info;
    /**
     * 种子info数据Hash
     */
    private final byte[] infoHash;
    /**
     * 种子info数据Hash（HEX小写）
     * 
     * @see #infoHash
     */
    private final String infoHashHex;
    /**
     * 种子info数据Hash（HTTP编码）
     * 
     * @see #infoHash
     */
    private final String infoHashUrl;
    
    /**
     * @param infoHash infoHash
     */
    private InfoHash(byte[] infoHash) {
        this.infoHash    = infoHash;
        this.infoHashHex = StringUtils.hex(this.infoHash);
        this.infoHashUrl = PeerUtils.urlEncode(this.infoHash);
    }

    /**
     * 生成InfoHash
     * 
     * @param data 种子Info
     * 
     * @return {@link InfoHash}
     */
    public static final InfoHash newInstance(byte[] data) {
        final InfoHash infoHash = new InfoHash(DigestUtils.sha1(data));
        infoHash.info = data;
        infoHash.size = data.length;
        return infoHash;
    }
    
    /**
     * 生成InfoHash
     * 
     * @param hash 种子info数据Hash
     * 
     * @return {@link InfoHash}
     * 
     * @throws DownloadException 下载异常
     */
    public static final InfoHash newInstance(String hash) throws DownloadException {
        if(StringUtils.isEmpty(hash)) {
            throw new DownloadException("不支持的Hash：" + hash);
        }
        hash = hash.strip();
        if(Protocol.Type.verifyMagnetHash40(hash)) {
            return new InfoHash(StringUtils.unhex(hash));
        } else if(Protocol.Type.verifyMagnetHash32(hash)) {
            return new InfoHash(Base32Utils.decode(hash));
        } else {
            throw new DownloadException("不支持的Hash：" + hash);
        }
    }
    
    /**
     * @return 种子info数据长度
     */
    public int getSize() {
        return this.size;
    }
    
    /**
     * @param size 种子info数据长度
     */
    public void setSize(int size) {
        this.size = size;
    }
    
    /**
     * @return 种子info数据
     */
    public byte[] getInfo() {
        return this.info;
    }
    
    /**
     * @param info 种子info数据
     */
    public void setInfo(byte[] info) {
        this.info = info;
    }

    /**
     * @return 种子info数据Hash
     */
    public byte[] getInfoHash() {
        return this.infoHash;
    }
    
    /**
     * @return 种子info数据Hash（HEX小写）
     */
    public String getInfoHashHex() {
        return this.infoHashHex;
    }
    
    /**
     * @return 种子info数据Hash（HTTP编码）
     */
    public String getInfoHashUrl() {
        return this.infoHashUrl;
    }
    
    @Override
    public String toString() {
        return BeanUtils.toString(this, this.infoHashHex);
    }
    
}
