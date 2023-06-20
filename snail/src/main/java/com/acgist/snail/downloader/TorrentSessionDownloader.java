package com.acgist.snail.downloader;

import com.acgist.snail.context.ITaskSession;
import com.acgist.snail.net.DownloadException;
import com.acgist.snail.net.NetException;
import com.acgist.snail.net.torrent.Magnet;
import com.acgist.snail.net.torrent.TorrentContext;
import com.acgist.snail.net.torrent.TorrentSession;
import com.acgist.snail.protocol.magnet.MagnetBuilder;

/**
 * BT任务下载器
 * 
 * @author acgist
 */
public abstract class TorrentSessionDownloader extends MultifileDownloader {
    
    /**
     * BT任务信息
     */
    protected TorrentSession torrentSession;
    
    /**
     * @param taskSession 任务信息
     */
    protected TorrentSessionDownloader(ITaskSession taskSession) {
        super(taskSession);
    }
    
    @Override
    public void open() throws NetException, DownloadException {
        // 不能在构造函数中初始化：防止种子被删除后还能点击下载
        this.torrentSession = this.loadTorrentSession();
        super.open();
    }
    
    /**
     * 加载BT任务信息
     * 
     * @return BT任务信息
     * 
     * @throws DownloadException 下载异常
     */
    protected TorrentSession loadTorrentSession() throws DownloadException {
        final Magnet magnet = MagnetBuilder.newInstance(this.taskSession.getUrl()).build();
        return TorrentContext.getInstance().newTorrentSession(magnet.getHash(), this.taskSession.getTorrent());
    }
    
    @Override
    protected boolean checkCompleted() {
        return this.torrentSession.checkCompleted();
    }

}
