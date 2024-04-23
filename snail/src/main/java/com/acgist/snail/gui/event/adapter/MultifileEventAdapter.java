package com.acgist.snail.gui.event.adapter;

import java.util.List;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

import com.acgist.snail.context.ITaskSession;
import com.acgist.snail.context.wrapper.DescriptionWrapper;
import com.acgist.snail.gui.GuiContext;
import com.acgist.snail.gui.GuiContext.Mode;
import com.acgist.snail.gui.event.GuiEventArgs;
import com.acgist.snail.logger.Logger;
import com.acgist.snail.logger.LoggerFactory;
import com.acgist.snail.net.DownloadException;
import com.acgist.snail.net.application.ApplicationMessage;
import com.acgist.snail.net.torrent.Torrent;
import com.acgist.snail.net.torrent.TorrentContext;
import com.acgist.snail.net.torrent.TorrentFile;
import com.acgist.snail.utils.StringUtils;

/**
 * GUI选择下载文件事件
 * 
 * @author acgist
 */
public class MultifileEventAdapter extends GuiEventArgs {

    private static final Logger LOGGER = LoggerFactory.getLogger(MultifileEventAdapter.class);
    
    /**
     * 选择下载文件列表（B编码）
     */
    private static final AtomicReference<String> FILES = new AtomicReference<>();
    
    public MultifileEventAdapter() {
        super(Type.MULTIFILE, "选择下载文件事件");
    }

    @Override
    protected final void executeExtend(GuiContext.Mode mode, Object ... args) {
        if(!this.check(args, 1)) {
            return;
        }
        final ITaskSession taskSession = (ITaskSession) this.getArg(args, 0);
        if(mode == Mode.NATIVE) {
            this.executeNativeExtend(taskSession);
        } else {
            this.executeExtendExtend(taskSession);
        }
    }
    
    /**
     * 本地消息
     * 
     * @param taskSession 任务信息
     */
    protected void executeNativeExtend(ITaskSession taskSession) {
        this.executeExtendExtend(taskSession);
    }
    
    /**
     * 扩展消息
     * 
     * @param taskSession 任务信息
     */
    protected void executeExtendExtend(ITaskSession taskSession) {
        String files = null;
        try {
            final Torrent torrent = TorrentContext.getInstance().newTorrentSession(taskSession.getTorrent()).torrent();
            // 没有选择文件
            while(StringUtils.isEmpty((files = FILES.get()))) {
                final List<String> downloadList = torrent.getInfo().files().stream()
                    .filter(TorrentFile::notPaddingFile)
                    .map(TorrentFile::path)
                    .collect(Collectors.toList());
                final String downloadFiles = DescriptionWrapper.newEncoder(downloadList).serialize();
                this.sendExtendGuiMessage(ApplicationMessage.Type.MULTIFILE.build(downloadFiles));
                MultifileEventAdapter.waitForFiles();
            }
            // 选择文件列表
            final List<String> selectFiles = DescriptionWrapper.newDecoder(files).deserialize().stream()
                .map(StringUtils::getString)
                .collect(Collectors.toList());
            // 选择文件大小
            final long size = torrent.getInfo().files().stream()
                .filter(file -> selectFiles.contains(file.path()))
                .collect(Collectors.summingLong(TorrentFile::getLength));
            taskSession.setSize(size);
            taskSession.setDescription(files);
        } catch (DownloadException e) {
            LOGGER.error("设置选择下载文件列表异常：{}", files, e);
        } finally {
            FILES.set(null);
        }
    }
    
    /**
     * 等待设置选择下载文件列表
     */
    private static final void waitForFiles() {
        synchronized (FILES) {
            try {
                FILES.wait(1000);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                LOGGER.error("等待设置选择下载文件列表异常", e);
            }
        }
    }
    
    /**
     * 设置选择下载文件列表（B编码）
     * 
     * @param files 选择下载文件列表（B编码）
     */
    public static final void files(String files) {
        FILES.set(files);
        synchronized (FILES) {
            FILES.notifyAll();
        }
    }
    
}
