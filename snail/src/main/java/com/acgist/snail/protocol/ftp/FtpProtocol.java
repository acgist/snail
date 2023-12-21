package com.acgist.snail.protocol.ftp;

import com.acgist.snail.context.ITaskSession;
import com.acgist.snail.downloader.IDownloader;
import com.acgist.snail.downloader.ftp.FtpDownloader;
import com.acgist.snail.net.DownloadException;
import com.acgist.snail.net.NetException;
import com.acgist.snail.net.ftp.FtpClient;
import com.acgist.snail.protocol.Protocol;

/**
 * FTP协议
 * 
 * @author acgist
 */
public final class FtpProtocol extends Protocol {
    
    private static final FtpProtocol INSTANCE = new FtpProtocol();
    
    public static final FtpProtocol getInstance() {
        return INSTANCE;
    }
    
    private FtpProtocol() {
        super(Type.FTP, "FTP");
    }
    
    @Override
    public boolean available() {
        return true;
    }
    
    @Override
    public IDownloader buildDownloader(ITaskSession taskSession) {
        return FtpDownloader.newInstance(taskSession);
    }

    @Override
    protected void buildSize() throws DownloadException {
        final FtpClient client = FtpClient.newInstance(this.url);
        try {
            client.connect();
            final long size = client.size();
            this.taskEntity.setSize(size);
        } catch (NetException e) {
            throw new DownloadException(e);
        } finally {
            client.close();
        }
    }

}
