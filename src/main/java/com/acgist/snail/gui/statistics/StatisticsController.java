package com.acgist.snail.gui.statistics;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.acgist.snail.gui.Controller;
import com.acgist.snail.gui.Tooltips;
import com.acgist.snail.net.torrent.TorrentManager;
import com.acgist.snail.net.torrent.dht.bootstrap.NodeManager;
import com.acgist.snail.net.torrent.peer.bootstrap.PeerManager;
import com.acgist.snail.net.torrent.tracker.bootstrap.TrackerClient;
import com.acgist.snail.net.torrent.tracker.bootstrap.TrackerManager;
import com.acgist.snail.pojo.session.NodeSession;
import com.acgist.snail.system.SystemStatistics;
import com.acgist.snail.utils.FileUtils;
import com.acgist.snail.utils.ObjectUtils;
import com.acgist.snail.utils.StringUtils;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.StackedBarChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;

/**
 * 统计窗口控制器
 * 
 * @author acgist
 * @since 1.2.0
 */
public final class StatisticsController extends Controller implements Initializable {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(StatisticsController.class);
	
	@FXML
	private Text upload;
	@FXML
	private Text download;
	@FXML
	private Text dhtTotal;
	@FXML
	private Text dhtAvailable;
	@FXML
	private Text dhtUnuse;
	@FXML
	private Text dhtVerify;
	@FXML
	private Text trackerTotal;
	@FXML
	private Text trackerAvailable;
	@FXML
	private Text trackerDisable;
	@FXML
	private Text peerTotal;
	@FXML
	private Text peerAvailable;
	@FXML
	private Text peerUtp;
	@FXML
	private Text sourceDht;
	@FXML
	private Text sourcePex;
	@FXML
	private Text sourceLsd;
	@FXML
	private Text sourceTracker;
	@FXML
	private Text sourceConnect;
	@FXML
	private Text sourceHolepunch;
	@FXML
	private Text statusUpload;
	@FXML
	private Text statusDownload;
	@FXML
	private Text health;
	@FXML
	private ChoiceBox<SelectInfoHash> selectInfoHashs;
	@FXML
	private VBox chart;
	
	@Override
	public void initialize(URL location, ResourceBundle resources) {
		this.selectInfoHashs.setOnAction(this.selectEvent);
	}

	/**
	 * 刷新按钮
	 */
	@FXML
	public void handleRefreshAction(ActionEvent event) {
		this.statistics();
	}
	
	/**
	 * 统计信息
	 */
	public void statistics() {
		this.system();
		this.dht();
		this.tracker();
		this.infoHash();
	}
	
	/**
	 * 释放资源
	 */
	public void release() {
		this.chart.getChildren().clear();
	}

	/**
	 * 统计系统信息：累计上传大小、累计下载大小
	 */
	private void system() {
		final var statistics = SystemStatistics.getInstance().statistics();
		this.upload.setText(FileUtils.formatSize(statistics.uploadSize()));
		this.download.setText(FileUtils.formatSize(statistics.downloadSize()));
	}
	
	/**
	 * 统计DHT信息：节点数量、各种状态节点数量
	 */
	private void dht() {
		final List<NodeSession> nodes = NodeManager.getInstance().nodes();
		final Map<NodeSession.Status, Long> group = nodes.stream()
			.collect(Collectors.groupingBy(NodeSession::getStatus, Collectors.counting()));
		this.dhtTotal.setText(String.valueOf(nodes.size()));
		this.dhtUnuse.setText(String.valueOf(group.getOrDefault(NodeSession.Status.UNUSE, 0L)));
		this.dhtVerify.setText(String.valueOf(group.getOrDefault(NodeSession.Status.VERIFY, 0L)));
		this.dhtAvailable.setText(String.valueOf(group.getOrDefault(NodeSession.Status.AVAILABLE, 0L)));
	}
	
	/**
	 * 统计Tracker信息：Tracker数量、各种状态Tracker数量
	 */
	private void tracker() {
		final List<TrackerClient> clients = TrackerManager.getInstance().clients();
		final Map<Boolean, Long> group = clients.stream()
			.collect(Collectors.groupingBy(TrackerClient::available, Collectors.counting()));
		this.trackerTotal.setText(String.valueOf(clients.size()));
		this.trackerAvailable.setText(String.valueOf(group.getOrDefault(Boolean.TRUE, 0L)));
		this.trackerDisable.setText(String.valueOf(group.getOrDefault(Boolean.FALSE, 0L)));
	}
	
	/**
	 * 统计BT任务信息
	 */
	private void infoHash() {
		final var defaultValue = this.selectInfoHashs.getValue();
		final ObservableList<SelectInfoHash> obs = FXCollections.observableArrayList();
		TorrentManager.getInstance().allTorrentSession().forEach(session -> {
			obs.add(new SelectInfoHash(session.infoHashHex(), session.name()));
		});
		this.selectInfoHashs.setItems(obs);
		if(defaultValue == null) {
			this.selectInfoHashs.getSelectionModel().select(0);
		} else {
			final int index = obs.indexOf(defaultValue);
			this.selectInfoHashs.getSelectionModel().select(index);
		}
	}
	
	/**
	 * 不需要显示调用：选择下载任务时自动刷新
	 */
	private void peer() {
		final SelectInfoHash value = (SelectInfoHash) this.selectInfoHashs.getValue();
		if(value == null) {
			return;
		}
		LOGGER.debug("统计信息：{}", value.getName());
		final String infoHashHex = value.getHash();
		// Peer
		final var peers = PeerManager.getInstance().listPeers(infoHashHex);
		final var utp = new AtomicInteger(0);
		final var available = new AtomicInteger(0);
		// 来源
		final var pex = new AtomicInteger(0);
		final var dht = new AtomicInteger(0);
		final var lsd = new AtomicInteger(0);
		final var tracker = new AtomicInteger(0);
		final var connect = new AtomicInteger(0);
		final var holepunch = new AtomicInteger(0);
		// 上传大小、下载大小
		final var upload = new AtomicInteger(0);
		final var download = new AtomicInteger(0);
		// 图表
		final List<String> activePeer = new ArrayList<>();
		final List<XYChart.Data<String, Number>> uploadPeer = new ArrayList<>();
		final List<XYChart.Data<String, Number>> downloadPeer = new ArrayList<>();
		peers.forEach(peer -> {
			if(peer.dht()) {
				dht.incrementAndGet();
			}
			if(peer.available()) {
				available.incrementAndGet();
			}
			if(peer.pex()) {
				pex.incrementAndGet();
			}
			if(peer.utp()) {
				utp.incrementAndGet();
			}
			if(peer.lsd()) {
				lsd.incrementAndGet();
			}
			if(peer.tracker()) {
				tracker.incrementAndGet();
			}
			if(peer.connect()) {
				connect.incrementAndGet();
			}
			if(peer.holepunch()) {
				holepunch.incrementAndGet();
			}
			if(peer.uploading()) {
				upload.incrementAndGet();
				if(!activePeer.contains(peer.host())) {
					activePeer.add(peer.host());
				}
				final double uploadSize = FileUtils.formatSizeMB(peer.statistics().uploadSize());
				final double downloadSize = FileUtils.formatSizeMB(peer.statistics().downloadSize());
				final XYChart.Data<String, Number> uploadData = new XYChart.Data<>(peer.host(), uploadSize);
				final XYChart.Data<String, Number> downloadData = new XYChart.Data<>(peer.host(), downloadSize);
				uploadPeer.add(uploadData);
				downloadPeer.add(downloadData);
			}
			if(peer.downloading()) {
				download.incrementAndGet();
				if(!activePeer.contains(peer.host())) {
					activePeer.add(peer.host());
				}
				final double uploadSize = FileUtils.formatSizeMB(peer.statistics().uploadSize());
				final double downloadSize = FileUtils.formatSizeMB(peer.statistics().downloadSize());
				final XYChart.Data<String, Number> uploadData = new XYChart.Data<>(peer.host(), uploadSize);
				final XYChart.Data<String, Number> downloadData = new XYChart.Data<>(peer.host(), downloadSize);
				uploadPeer.add(uploadData);
				downloadPeer.add(downloadData);
			}
		});
		final var torrentSession = TorrentManager.getInstance().torrentSession(infoHashHex);
		// Peer
		this.peerTotal.setText(String.valueOf(peers.size()));
		this.peerUtp.setText(String.valueOf(utp.get()));
		this.peerAvailable.setText(String.valueOf(available.get()));
		// 来源
		this.sourceDht.setText(String.valueOf(dht.get()));
		this.sourcePex.setText(String.valueOf(pex.get()));
		this.sourceLsd.setText(String.valueOf(lsd.get()));
		this.sourceTracker.setText(String.valueOf(tracker.get()));
		this.sourceConnect.setText(String.valueOf(connect.get()));
		this.sourceHolepunch.setText(String.valueOf(holepunch.get()));
		// 上传大小、下载大小
		this.statusUpload.setText(String.valueOf(upload.get()));
		this.statusDownload.setText(String.valueOf(download.get()));
		this.health.setText(torrentSession.health() + "%");
		// 图表
		final CategoryAxis xAxis = new CategoryAxis();
		xAxis.setLabel(
			String.format(
				"累计上传：%s 累计下载：%s",
				FileUtils.formatSize(torrentSession.statistics().uploadSize()),
				FileUtils.formatSize(torrentSession.statistics().downloadSize())
			)
		);
		xAxis.setCategories(FXCollections.observableArrayList(activePeer));
		final NumberAxis yAxis = new NumberAxis();
		yAxis.setLabel("流量（MB）");
		final StackedBarChart<String, Number> stackedBarChart = new StackedBarChart<>(xAxis, yAxis);
		stackedBarChart.setPrefWidth(800);
		stackedBarChart.setPrefHeight(400);
		stackedBarChart.setTitle("Peer流量统计");
		final XYChart.Series<String, Number> uploadSeries = new XYChart.Series<>();
		uploadSeries.setName("上传");
		uploadSeries.getData().addAll(uploadPeer);
		final XYChart.Series<String, Number> downloadSeries = new XYChart.Series<>();
		downloadSeries.setName("下载");
		downloadSeries.getData().addAll(downloadPeer);
		stackedBarChart.getData().add(uploadSeries);
		stackedBarChart.getData().add(downloadSeries);
//		stackedBarChart.getData().addAll(List.of(uploadSeries, downloadSeries));
		uploadPeer.forEach(data -> {
			Tooltip.install(data.getNode(), Tooltips.newTooltip(String.format("IP：%s 上传：%.2fMB", data.getXValue(), data.getYValue())));
		});
		downloadPeer.forEach(data -> {
			Tooltip.install(data.getNode(), Tooltips.newTooltip(String.format("IP：%s 下载：%.2fMB", data.getXValue(), data.getYValue())));
		});
		this.chart.getChildren().clear();
		this.chart.getChildren().add(stackedBarChart);
	}
	
	/**
	 * 选择BT任务事件
	 */
	private EventHandler<ActionEvent> selectEvent = (event) -> {
		this.peer();
	};
	
	/**
	 * 下载任务
	 */
	public static final class SelectInfoHash {

		private final String hash; // 任务HASH
		private final String name; // 任务名称

		public SelectInfoHash(String hash, String name) {
			this.hash = hash;
			this.name = name;
		}

		public String getHash() {
			return hash;
		}

		public String getName() {
			return name;
		}
		
		@Override
		public int hashCode() {
			return ObjectUtils.hashCode(this.hash);
		}
		
		@Override
		public boolean equals(Object object) {
			if(ObjectUtils.equals(this, object)) {
				return true;
			}
			if(object instanceof SelectInfoHash) {
				final SelectInfoHash selectInfoHash = (SelectInfoHash) object;
				return StringUtils.equals(this.hash, selectInfoHash.hash);
			}
			return false;
		}
		
		/**
		 * <p>重写toString设置下拉框显示名称</p>
		 * <p>或者使用<code>this.selectInfoHashs.converterProperty().set</code>来设置</p>
		 */
		@Override
		public String toString() {
			return this.name;
		}
		
	}

}
