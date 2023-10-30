package com.acgist.snail.net.torrent;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.junit.jupiter.api.Test;

import com.acgist.snail.context.ITaskSessionStatus.Status;
import com.acgist.snail.context.entity.TaskEntity;
import com.acgist.snail.context.session.TaskSession;
import com.acgist.snail.context.wrapper.DescriptionWrapper;
import com.acgist.snail.logger.LoggerConfig;
import com.acgist.snail.net.DownloadException;
import com.acgist.snail.net.NetException;
import com.acgist.snail.protocol.Protocol.Type;
import com.acgist.snail.utils.Performance;
import com.acgist.snail.utils.ThreadUtils;

class TorrentStreamGroupTest extends Performance {

    @Test
    void testReload() throws DownloadException {
        final String path = "D:/tmp/snail/ebfc2cf2ce69ba2f7aea36bbef290f0cce21386c.torrent";
        final TorrentSession session = TorrentContext.getInstance().newTorrentSession(path);
        final TaskEntity entity = new TaskEntity();
        entity.setFile("D:/tmp/snail/reload/");
        entity.setType(Type.TORRENT);
        entity.setName("acgist");
        entity.setStatus(Status.COMPLETED);
        final List<TorrentFile> files = session.torrent().getInfo().files().stream()
            .filter(TorrentFile::notPaddingFile)
            .collect(Collectors.toList());
        final List<String> list = new ArrayList<>();
        // 加载MKV文件
        files.forEach(file -> {
            if(file.path().endsWith("mkv")) {
                file.selected(true);
                list.add(file.path());
            }
        });
        entity.setDescription(DescriptionWrapper.newEncoder(list).serialize());
        session.upload(TaskSession.newInstance(entity));
        final TorrentStreamGroup group = session.torrentStreamGroup();
        // 加载空文件
        ThreadUtils.sleep(1000);
        group.reload(entity.getFile(), files);
        files.forEach(file -> {
            if(file.path().endsWith("png")) {
                file.selected(true);
            }
        });
        // 加载PNG文件
        ThreadUtils.sleep(1000);
        group.reload(entity.getFile(), files);
        files.forEach(file -> {
            if(file.path().endsWith("png")) {
                file.selected(false);
            }
        });
        // 卸载PNG文件
        ThreadUtils.sleep(1000);
        group.reload(entity.getFile(), files);
        assertNotNull(group);
    }
    
    @Test
    void testPick() throws DownloadException {
        LoggerConfig.off();
        final String path = "D:/tmp/snail/ebfc2cf2ce69ba2f7aea36bbef290f0cce21386c.torrent";
        final TorrentSession session = TorrentContext.getInstance().newTorrentSession(path);
        final TaskEntity entity = new TaskEntity();
        entity.setFile("D:/tmp/snail/pick/");
        entity.setType(Type.TORRENT);
        entity.setName("acgist");
        entity.setStatus(Status.AWAIT);
        final List<String> list = new ArrayList<>();
        session.torrent().getInfo().files().stream()
            .filter(TorrentFile::notPaddingFile)
            .forEach(file -> {
                file.selected(true);
                list.add(file.path());
            });
        entity.setDescription(DescriptionWrapper.newEncoder(list).serialize());
        session.upload(TaskSession.newInstance(entity));
        final TorrentStreamGroup group = session.torrentStreamGroup();
        final BitSet peerPieces = new BitSet();
        peerPieces.set(0, session.torrent().getInfo().pieceSize(), true);
        final BitSet suggestPieces = new BitSet();
        this.cost();
        TorrentPiece index;
        final Set<Integer> indexSet = new HashSet<Integer>();
        while((index = group.pick(peerPieces, suggestPieces)) != null) {
            this.log(index);
            group.done(index.getIndex());
            indexSet.add(index.getIndex());
        }
        assertEquals(session.torrent().getInfo().pieceSize(), indexSet.size());
        this.costed();
    }
    
    @Test
    void testVerify() throws DownloadException, NetException, IOException {
        final String path = "D:/tmp/snail/ebfc2cf2ce69ba2f7aea36bbef290f0cce21386c.torrent";
        final TorrentSession session = TorrentContext.getInstance().newTorrentSession(path);
        final TaskEntity entity = new TaskEntity();
        entity.setFile("D:/tmp/snail/server/");
        entity.setType(Type.TORRENT);
        entity.setName("acgist");
        entity.setStatus(Status.COMPLETED);
        final List<String> list = new ArrayList<>();
        // 加载MKV文件
        session.torrent().getInfo().files().stream()
            .filter(TorrentFile::notPaddingFile)
            .forEach(file -> {
                if(file.path().contains("Scans/Vol.1")) {
                    file.selected(true);
                    list.add(file.path());
                }
            });
        entity.setDescription(DescriptionWrapper.newEncoder(list).serialize());
        session.upload(TaskSession.newInstance(entity));
        final TorrentStreamGroup group = session.torrentStreamGroup();
        assertTrue(group.verify());
    }

}
