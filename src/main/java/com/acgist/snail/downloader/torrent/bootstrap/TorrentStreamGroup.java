package com.acgist.snail.downloader.torrent.bootstrap;

import java.io.IOException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.acgist.snail.protocol.torrent.bean.Torrent;
import com.acgist.snail.protocol.torrent.bean.TorrentFile;
import com.acgist.snail.protocol.torrent.bean.TorrentInfo;
import com.acgist.snail.utils.CollectionUtils;

/**
 * 文件组
 */
public class TorrentStreamGroup {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(TorrentStreamGroup.class);

	/**
	 * 块数量
	 */
	private int pieceSize;
	/**
	 * 已下载的块位图
	 */
	private BitSet pieces;
	/**
	 * 文件流
	 */
	private List<TorrentStream> streams;

	public static final TorrentStreamGroup newInstance(String folder, Torrent torrent, List<TorrentFile> files) {
		TorrentStreamGroup group = new TorrentStreamGroup();
		if(CollectionUtils.isNotEmpty(files)) {
			long pos = 0;
			final List<TorrentStream> streams = new ArrayList<>(files.size());
			for (TorrentFile file : files) {
				TorrentInfo info = torrent.getInfo();
				group.pieceSize = info.pieceSize();
				try {
					pos+=file.getLength();
					if(file.selected()) {
						TorrentStream stream = new TorrentStream(info.getPieceLength(), group);
						stream.buildFile(Paths.get(folder + file.path()).toString(), file.getLength(), pos);
						streams.add(stream);
					}
				} catch (IOException e) {
					LOGGER.error("创建文件异常", e);
				}
			}
			group.streams = streams;
		}
		return group;
	}
	
	private TorrentStreamGroup() {
	}

	/**
	 * 挑选一个下载
	 * 设置为已经下载
	 */
	public int pick(int index) {
		synchronized (this) {
			int pickIndex = -1;
			while(true) {
				pickIndex = nextPiece(index);
				if(!downloading(pickIndex)) {
					break;
				}
			}
			if(pickIndex > pieceSize) {
				return -1;
			}
			return pickIndex;
		}
	}

	/**
	 * 是否在下载中
	 */
	private boolean downloading(int index) {
		for (TorrentStream torrentStream : streams) {
			if(torrentStream.downloading(index)) {
				return true;
			}
		}
		return false;
	}

	/**
	 * 获取下一个未下载的Piece
	 */
	public int nextPiece(int index) {
		return pieces.nextClearBit(index);
	}

	/**
	 * 设置已下载的Piece
	 */
	public void piece(int index) {
		pieces.set(index, true);
	}

	/**
	 * 获取下载文件路径
	 * @param folder 文件地址
	 * @param path 文件路径
	 */
	private static final String file(String folder, String path) {
//		Paths.get(first, more)
		return null;
	}
	
}
