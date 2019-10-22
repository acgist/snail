package com.acgist.snail.gui.statistics;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import com.acgist.snail.gui.Controller;
import com.acgist.snail.gui.Tooltips;
import com.acgist.snail.net.torrent.TorrentManager;
import com.acgist.snail.net.torrent.dht.bootstrap.NodeManager;
import com.acgist.snail.net.torrent.peer.bootstrap.PeerManager;
import com.acgist.snail.net.torrent.tracker.bootstrap.TrackerClient;
import com.acgist.snail.net.torrent.tracker.bootstrap.TrackerManager;
import com.acgist.snail.pojo.session.NodeSession;
import com.acgist.snail.system.context.SystemStatistics;
import com.acgist.snail.utils.CollectionUtils;
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
public class StatisticsController extends Controller implements Initializable {
	
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
	private Text statusUpload;
	@FXML
	private Text statusDownload;
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
	
	public void statistics() {
		this.system();
		this.dht();
		this.tracker();
		this.infoHash();
		this.peer();
	}
	
	/**
	 * 释放资源
	 */
	public void release() {
		this.chart.getChildren().clear();
	}

	private void system() {
		final var statistics = SystemStatistics.getInstance().getSystemStatistics();
		this.upload.setText(FileUtils.formatSize(statistics.uploadSize()));
		this.download.setText(FileUtils.formatSize(statistics.downloadSize()));
	}
	
	private void dht() {
		final List<NodeSession> nodes = NodeManager.getInstance().nodes();
		final Map<NodeSession.Status, Long> group = nodes.stream()
			.collect(Collectors.groupingBy(NodeSession::getStatus, Collectors.counting()));
		this.dhtTotal.setText(String.valueOf(nodes.size()));
		this.dhtUnuse.setText(String.valueOf(group.getOrDefault(NodeSession.Status.unuse, 0L)));
		this.dhtVerify.setText(String.valueOf(group.getOrDefault(NodeSession.Status.verify, 0L)));
		this.dhtAvailable.setText(String.valueOf(group.getOrDefault(NodeSession.Status.available, 0L)));
	}
	
	private void tracker() {
		final List<TrackerClient> clients = TrackerManager.getInstance().clients();
		final Map<Boolean, Long> group = clients.stream()
			.collect(Collectors.groupingBy(TrackerClient::available, Collectors.counting()));
		this.trackerTotal.setText(String.valueOf(clients.size()));
		this.trackerAvailable.setText(String.valueOf(group.getOrDefault(Boolean.TRUE, 0L)));
		this.trackerDisable.setText(String.valueOf(group.getOrDefault(Boolean.FALSE, 0L)));
	}
	
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
	
	private void peer() {
		final SelectInfoHash value = (SelectInfoHash) this.selectInfoHashs.getValue();
		if(value == null) {
			return;
		}
		final String infoHashHex = value.getHash();
		// Peer
		final var peers = PeerManager.getInstance().listPeers(infoHashHex);
		if(CollectionUtils.isEmpty(peers)) {
			return;
		}
		final var utp = new AtomicInteger(0);
		final var available = new AtomicInteger(0);
		// 来源
		final var dht = new AtomicInteger(0);
		final var pex = new AtomicInteger(0);
		final var lsd = new AtomicInteger(0);
		final var tracker = new AtomicInteger(0);
		final var connect = new AtomicInteger(0);
		// 状态
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
			if(peer.pex()) {
				pex.incrementAndGet();
			}
			if(peer.lsd()) {
				lsd.incrementAndGet();
			}
			if(peer.utp()) {
				utp.incrementAndGet();
			}
			if(peer.tracker()) {
				tracker.incrementAndGet();
			}
			if(peer.connect()) {
				connect.incrementAndGet();
			}
			if(peer.uploading()) {
				upload.incrementAndGet();
				if(!activePeer.contains(peer.host())) {
					activePeer.add(peer.host());
				}
				final XYChart.Data<String, Number> data = new XYChart.Data<>(peer.host(), FileUtils.formatSizeMB(peer.statistics().uploadSize()));
				Tooltip.install(data.getNode(), Tooltips.newTooltip(peer.host()));
				uploadPeer.add(data);
			}
			if(peer.downloading()) {
				download.incrementAndGet();
				if(!activePeer.contains(peer.host())) {
					activePeer.add(peer.host());
				}
				final XYChart.Data<String, Number> data = new XYChart.Data<>(peer.host(), FileUtils.formatSizeMB(peer.statistics().downloadSize()));
				Tooltip.install(data.getNode(), Tooltips.newTooltip(peer.host()));
				downloadPeer.add(data);
			}
			if(peer.available()) {
				available.incrementAndGet();
			}
		});
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
		// 状态
		this.statusUpload.setText(String.valueOf(upload.get()));
		this.statusDownload.setText(String.valueOf(download.get()));
		// 图表
		CategoryAxis xAxis = new CategoryAxis();
		xAxis.setLabel("Peer");
		xAxis.setCategories(FXCollections.observableArrayList(activePeer));
		NumberAxis yAxis = new NumberAxis();
		yAxis.setLabel("流量（MB）");
		StackedBarChart<String, Number> stackedBarChart = new StackedBarChart<>(xAxis, yAxis);
		stackedBarChart.setTitle("Peer流量");
		XYChart.Series<String, Number> uploadSeries = new XYChart.Series<>();
		uploadSeries.setName("上传");
		uploadSeries.getData().addAll(uploadPeer);
		XYChart.Series<String, Number> downloadSeries = new XYChart.Series<>();
		downloadSeries.setName("下载");
		downloadSeries.getData().addAll(downloadPeer);
		stackedBarChart.getData().add(uploadSeries);
		stackedBarChart.getData().add(downloadSeries);
//		stackedBarChart.getData().addAll(List.of(uploadSeries, downloadSeries));
		stackedBarChart.setPrefWidth(800);
		stackedBarChart.setPrefHeight(400);
		this.chart.getChildren().clear();
		this.chart.getChildren().add(stackedBarChart);
	}
	
	/**
	 * 选择InfoHash事件
	 */
	private EventHandler<ActionEvent> selectEvent = (event) -> {
		this.peer();
	};
	
	public static final class SelectInfoHash {

		private final String hash;
		private final String name;

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
		 * <p>也可以使用`this.infoHashs.converterProperty().set`来设置下拉框显示名称</p>
		 */
		@Override
		public String toString() {
			return this.name;
		}
		
	}

}
