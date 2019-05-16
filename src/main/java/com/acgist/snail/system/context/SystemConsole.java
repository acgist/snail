package com.acgist.snail.system.context;

import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.acgist.snail.net.tracker.bootstrap.TrackerClient;
import com.acgist.snail.pojo.session.NodeSession;
import com.acgist.snail.pojo.session.PeerSession;
import com.acgist.snail.system.manager.NodeManager;
import com.acgist.snail.system.manager.PeerManager;
import com.acgist.snail.system.manager.TrackerManager;
import com.acgist.snail.utils.FileUtils;

/**
 * 系统控制台
 */
public class SystemConsole {

	private static final Logger LOGGER = LoggerFactory.getLogger(SystemConsole.class);
	
	private static final SystemConsole INSTANCE = new SystemConsole();

	private static final String NEW_LINE = "\r\n";
	
	private StringBuilder builder = new StringBuilder(NEW_LINE);
	
	private SystemConsole() {
	}

	public static final SystemConsole getInstance() {
		return INSTANCE;
	}
	
	/**
	 * 输出系统状态
	 */
	public synchronized void console() {
		system();
		node();
		tracker();
		peer();
		LOGGER.info("系统状态：{}", builder.toString());
		builder.setLength(0);
		builder = new StringBuilder(NEW_LINE);
	}
	
	/**
	 * 系统状态
	 */
	private void system() {
		var statistics = SystemStatistics.getInstance().getSystemStatistics();
		builder.append("累计上传：").append(FileUtils.formatSize(statistics.uploadSize())).append(NEW_LINE);
		builder.append("累计下载：").append(FileUtils.formatSize(statistics.downloadSize())).append(NEW_LINE);
	}
	
	/**
	 * Node
	 */
	private void node() {
		final List<NodeSession> nodes = NodeManager.getInstance().nodes();
		final Map<Byte, Long> group = nodes.stream().collect(Collectors.groupingBy(NodeSession::getStatus, Collectors.counting()));
		builder.append("Node数量：").append(nodes.size()).append(NEW_LINE);
		builder.append("Node数量（未使用）：").append(group.get(NodeSession.STATUS_UNUSE)).append(NEW_LINE);
		builder.append("Node数量（使用中）：").append(group.get(NodeSession.STATUS_VERIFY)).append(NEW_LINE);
		builder.append("Node数量（有效）：").append(group.get(NodeSession.STATUS_AVAILABLE)).append(NEW_LINE);
	}
	
	/**
	 * Tracker
	 */
	private void tracker() {
		final List<TrackerClient> clients = TrackerManager.getInstance().clients();
		final Map<Boolean, Long> group = clients.stream().collect(Collectors.groupingBy(TrackerClient::available, Collectors.counting()));
		builder.append("Tracker数量：").append(clients.size()).append(NEW_LINE);
		builder.append("Tracker数量（可用）：").append(group.get(Boolean.TRUE)).append(NEW_LINE);
		builder.append("Tracker数量（不可用）：").append(group.get(Boolean.FALSE)).append(NEW_LINE);
	}
	
	/**
	 * Peer
	 */
	private void peer() {
		final Map<String, List<PeerSession>> peers = PeerManager.getInstance().peers();
		final var dht = new AtomicInteger(0);
		final var pex = new AtomicInteger(0);
		final var tracker = new AtomicInteger(0);
		final var connect = new AtomicInteger(0);
		final var upload = new AtomicInteger(0);
		final var download = new AtomicInteger(0);
		final var available = new AtomicInteger(0);
		peers.entrySet().stream()
		.filter(entry -> entry.getValue() != null)
		.forEach(entry -> {
			final var list = entry.getValue();
			list.forEach(peer -> {
				if(peer.dht()) {
					dht.incrementAndGet();
				}
				if(peer.pex()) {
					pex.incrementAndGet();
				}
				if(peer.tracker()) {
					tracker.incrementAndGet();
				}
				if(peer.connect()) {
					connect.incrementAndGet();
				}
				if(peer.uploading()) {
					upload.incrementAndGet();
				}
				if(peer.downloading()) {
					download.incrementAndGet();
				}
				if(peer.available()) {
					available.incrementAndGet();
				}
			});
			builder
				.append("Peer InfoHashHex：").append(entry.getKey()).append("，")
				.append("Peer数量：").append(list.size()).append("，")
				.append("Peer数量（可用）：").append(available.getAndSet(0)).append("，")
				.append("来源：")
				.append("DHT-").append(dht.getAndSet(0)).append("、")
				.append("PEX-").append(pex.getAndSet(0)).append("、")
				.append("Tracker-").append(tracker.getAndSet(0)).append("、")
				.append("Connect-").append(connect.getAndSet(0)).append("，")
				.append("上传中：").append(upload.getAndSet(0)).append("，")
				.append("下载中：").append(download.getAndSet(0)).append(NEW_LINE);
		});
	}

}
