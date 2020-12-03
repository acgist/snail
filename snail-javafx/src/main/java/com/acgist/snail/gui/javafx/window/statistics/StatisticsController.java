package com.acgist.snail.gui.javafx.window.statistics;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.acgist.snail.config.SystemConfig;
import com.acgist.snail.context.NatContext;
import com.acgist.snail.context.SystemStatistics;
import com.acgist.snail.gui.javafx.Alerts;
import com.acgist.snail.gui.javafx.Controller;
import com.acgist.snail.gui.javafx.Tooltips;
import com.acgist.snail.net.torrent.TorrentManager;
import com.acgist.snail.net.torrent.dht.bootstrap.NodeManager;
import com.acgist.snail.net.torrent.peer.bootstrap.PeerManager;
import com.acgist.snail.net.torrent.tracker.bootstrap.TrackerClient;
import com.acgist.snail.net.torrent.tracker.bootstrap.TrackerManager;
import com.acgist.snail.pojo.session.NodeSession;
import com.acgist.snail.utils.FileUtils;
import com.acgist.snail.utils.NetUtils;
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
import javafx.scene.chart.PieChart;
import javafx.scene.chart.StackedBarChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Label;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;

/**
 * <p>统计窗口控制器</p>
 * 
 * @author acgist
 * @since 1.2.0
 */
public final class StatisticsController extends Controller implements Initializable {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(StatisticsController.class);
	
	/**
	 * <p>图表宽度：{@value}</p>
	 */
	private static final int CHART_WIDTH = 800;
	/**
	 * <p>图表高度：{@value}</p>
	 */
	private static final int CHART_HEIGHT = 400;
	
	/**
	 * <p>统计信息筛选</p>
	 */
	public enum Filter {
		
		/** 系统信息 */
		SYSTEM,
		/** PEER统计 */
		PEER,
		/** 流量统计 */
		TRAFFIC,
		/** 下载统计 */
		PIECE;
		
	}
	
	@FXML
	private FlowPane root;
	
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
	private ChoiceBox<SelectInfoHash> selectInfoHashs;
	@FXML
	private VBox statisticsBox;
	
	private Filter filter = Filter.SYSTEM;
	
	@Override
	public void initialize(URL location, ResourceBundle resources) {
		this.selectInfoHashs.setOnAction(this.selectEvent);
	}
	
	/**
	 * <p>刷新按钮</p>
	 * 
	 * @param event 事件
	 */
	@FXML
	public void handleRefreshAction(ActionEvent event) {
		this.statistics();
	}
	
	/**
	 * <p>系统信息</p>
	 * 
	 * @param event 事件
	 */
	@FXML
	public void handleSystemAction(ActionEvent event) {
		this.filter = Filter.SYSTEM;
		this.system();
	}
	
	/**
	 * <p>Peer统计</p>
	 * 
	 * @param event 事件
	 */
	@FXML
	public void handlePeerAction(ActionEvent event) {
		this.filter = Filter.PEER;
		this.peer();
	}
	
	/**
	 * <p>流量统计</p>
	 * 
	 * @param event 事件
	 */
	@FXML
	public void handleTrafficAction(ActionEvent event) {
		this.filter = Filter.TRAFFIC;
		this.traffic();
	}
	
	/**
	 * <p>下载统计</p>
	 * 
	 * @param event 事件
	 */
	@FXML
	public void handlePieceAction(ActionEvent event) {
		this.filter = Filter.PIECE;
		this.piece();
	}
	
	/**
	 * <p>统计信息</p>
	 * 
	 * @see #systemTraffic()
	 * @see #dht()
	 * @see #tracker()
	 * @see #infoHash()
	 */
	public void statistics() {
		this.systemTraffic();
		this.dht();
		this.tracker();
		this.infoHash();
	}
	
	/**
	 * <p>释放资源</p>
	 */
	public void release() {
		this.statisticsBox.getChildren().clear();
	}

	/**
	 * <p>统计系统流量信息</p>
	 */
	private void systemTraffic() {
		final var statistics = SystemStatistics.getInstance().statistics();
		// 累计上传
		this.upload.setText(FileUtils.formatSize(statistics.uploadSize()));
		// 累计下载
		this.download.setText(FileUtils.formatSize(statistics.downloadSize()));
	}
	
	/**
	 * <p>统计DHT信息</p>
	 */
	private void dht() {
		final List<NodeSession> nodes = NodeManager.getInstance().nodes();
		final Map<NodeSession.Status, Long> nodeGroup = nodes.stream()
			.collect(Collectors.groupingBy(NodeSession::getStatus, Collectors.counting()));
		this.dhtTotal.setText(String.valueOf(nodes.size()));
		this.dhtUnuse.setText(String.valueOf(nodeGroup.getOrDefault(NodeSession.Status.UNUSE, 0L)));
		this.dhtVerify.setText(String.valueOf(nodeGroup.getOrDefault(NodeSession.Status.VERIFY, 0L)));
		this.dhtAvailable.setText(String.valueOf(nodeGroup.getOrDefault(NodeSession.Status.AVAILABLE, 0L)));
	}
	
	/**
	 * <p>统计Tracker信息</p>
	 */
	private void tracker() {
		final List<TrackerClient> clients = TrackerManager.getInstance().clients();
		final Map<Boolean, Long> clientGroup = clients.stream()
			.collect(Collectors.groupingBy(TrackerClient::available, Collectors.counting()));
		this.trackerTotal.setText(String.valueOf(clients.size()));
		this.trackerDisable.setText(String.valueOf(clientGroup.getOrDefault(Boolean.FALSE, 0L)));
		this.trackerAvailable.setText(String.valueOf(clientGroup.getOrDefault(Boolean.TRUE, 0L)));
	}
	
	/**
	 * <p>BT任务选择</p>
	 */
	private void infoHash() {
		final var defaultValue = this.selectInfoHashs.getValue();
		final ObservableList<SelectInfoHash> obs = FXCollections.observableArrayList();
		TorrentManager.getInstance().allTorrentSession().stream()
			.filter(session -> session.done()) // 准备完成
			.forEach(session -> obs.add(new SelectInfoHash(session.infoHashHex(), session.name())));
		this.selectInfoHashs.setItems(obs);
		if(defaultValue == null) {
			// 没有选中任务：默认选中第一个任务
			this.selectInfoHashs.getSelectionModel().select(0);
		} else {
			// 已经选中任务：选中之前选中的任务
			final int index = obs.indexOf(defaultValue);
			this.selectInfoHashs.getSelectionModel().select(index);
		}
	}

	/**
	 * <p>系统信息</p>
	 */
	public void system() {
		final VBox systemInfo = new VBox(
			this.buildSystemInfo("本机IP", NetUtils.localHostAddress()),
			this.buildSystemInfo("外网IP", SystemConfig.getExternalIpAddress()),
			this.buildSystemInfo("外网端口", SystemConfig.getTorrentPortExt()),
			this.buildSystemInfo("内网穿透", NatContext.getInstance().type()),
			this.buildSystemInfo("软件版本", SystemConfig.getVersion()),
			this.buildSystemInfo("系统名称", System.getProperty("os.name")),
			this.buildSystemInfo("系统版本", System.getProperty("os.version")),
			this.buildSystemInfo("Java版本", System.getProperty("java.version")),
			this.buildSystemInfo("虚拟机名称", System.getProperty("java.vm.name"))
		);
		systemInfo.getStyleClass().add("system-info");
		this.statisticsBox.getChildren().clear();
		this.statisticsBox.getChildren().add(systemInfo);
	}
	
	/**
	 * <p>创建系统信息节点</p>
	 * 
	 * @param name 名称
	 * @param info 信息
	 * 
	 * @return 节点
	 */
	private TextFlow buildSystemInfo(String name, Object info) {
		final Label label = new Label(name);
		Text text;
		if(info == null) {
			text = new Text("获取失败");
		} else {
			text = new Text(info.toString());
		}
		return new TextFlow(label, text);
	}
	
	/**
	 * <p>Peer统计</p>
	 */
	private void peer() {
		final String infoHashHex = this.selectInfoHashHex();
		if(infoHashHex == null) {
			return;
		}
		final var peers = PeerManager.getInstance().listPeerSession(infoHashHex);
		// Peer
		final var utp = new AtomicInteger(0);
		final var available = new AtomicInteger(0);
		// 来源数量
		final var pex = new AtomicInteger(0);
		final var dht = new AtomicInteger(0);
		final var lsd = new AtomicInteger(0);
		final var tracker = new AtomicInteger(0);
		final var connect = new AtomicInteger(0);
		final var holepunch = new AtomicInteger(0);
		// 上传数量、下载数量
		final var upload = new AtomicInteger(0);
		final var download = new AtomicInteger(0);
		peers.forEach(peer -> {
			if(peer.utp()) {
				utp.incrementAndGet();
			}
			if(peer.available()) {
				available.incrementAndGet();
			}
			if(peer.fromDht()) {
				dht.incrementAndGet();
			}
			if(peer.fromPex()) {
				pex.incrementAndGet();
			}
			if(peer.fromLsd()) {
				lsd.incrementAndGet();
			}
			if(peer.fromTacker()) {
				tracker.incrementAndGet();
			}
			if(peer.fromConnect()) {
				connect.incrementAndGet();
			}
			if(peer.fromHolepunch()) {
				holepunch.incrementAndGet();
			}
			if(peer.uploading()) {
				upload.incrementAndGet();
			}
			if(peer.downloading()) {
				download.incrementAndGet();
			}
		});
		// 来源图表
		final ObservableList<PieChart.Data> pieChartData = FXCollections.observableArrayList(
			new PieChart.Data("DHT", dht.get()),
			new PieChart.Data("PEX", pex.get()),
			new PieChart.Data("LSD", lsd.get()),
			new PieChart.Data("Tracker", tracker.get()),
			new PieChart.Data("Connect", connect.get()),
			new PieChart.Data("Holepunch", holepunch.get())
		);
		final PieChart pieChart = new PieChart(pieChartData);
		pieChart.setTitle(
			String.format(
				"总量：%d 可用：%d 下载：%d 上传：%d",
				peers.size(), available.get(), download.get(), upload.get()
			)
		);
		// 设置提示信息
		pieChartData.forEach(data -> {
			Tooltip.install(data.getNode(), Tooltips.newTooltip(String.format("%s：%.0f", data.getName(), data.getPieValue())));
		});
		pieChart.setPrefWidth(CHART_WIDTH);
		pieChart.setPrefHeight(CHART_HEIGHT);
		this.statisticsBox.getChildren().clear();
		this.statisticsBox.getChildren().add(pieChart);
		LOGGER.debug(
			"Peer统计，总量：{}、可用：{}、下载：{}、上传：{}",
			peers.size(), available.get(), download.get(), upload.get()
		);
		LOGGER.debug(
			"Peer来源，DHT：{}、PEX：{}、LSD：{}、Tracker：{}、Connect：{}、Holepunch：{}",
			dht.get(), pex.get(), lsd.get(), tracker.get(), connect.get(), holepunch.get()
		);
	}
	
	/**
	 * <p>流量统计</p>
	 */
	private void traffic() {
		final String infoHashHex = this.selectInfoHashHex();
		if(infoHashHex == null) {
			return;
		}
		final var peers = PeerManager.getInstance().listPeerSession(infoHashHex);
		final var torrentSession = TorrentManager.getInstance().torrentSession(infoHashHex);
		// Peer分类
		final List<String> categoriesPeer = new ArrayList<>();
		// 上传流量
		final List<XYChart.Data<String, Number>> uploadPeer = new ArrayList<>();
		// 下载流量
		final List<XYChart.Data<String, Number>> downloadPeer = new ArrayList<>();
		peers.forEach(peer -> {
			if(peer.uploading()) {
				if(!categoriesPeer.contains(peer.host())) {
					categoriesPeer.add(peer.host());
				}
				final double uploadSize = FileUtils.formatSizeMB(peer.statistics().uploadSize());
				final double downloadSize = FileUtils.formatSizeMB(peer.statistics().downloadSize());
				final XYChart.Data<String, Number> uploadData = new XYChart.Data<>(peer.host(), uploadSize);
				final XYChart.Data<String, Number> downloadData = new XYChart.Data<>(peer.host(), downloadSize);
				uploadPeer.add(uploadData);
				downloadPeer.add(downloadData);
			}
			if(peer.downloading()) {
				if(!categoriesPeer.contains(peer.host())) {
					categoriesPeer.add(peer.host());
				}
				final double uploadSize = FileUtils.formatSizeMB(peer.statistics().uploadSize());
				final double downloadSize = FileUtils.formatSizeMB(peer.statistics().downloadSize());
				final XYChart.Data<String, Number> uploadData = new XYChart.Data<>(peer.host(), uploadSize);
				final XYChart.Data<String, Number> downloadData = new XYChart.Data<>(peer.host(), downloadSize);
				uploadPeer.add(uploadData);
				downloadPeer.add(downloadData);
			}
		});
		// X轴
		final CategoryAxis xAxis = new CategoryAxis();
		xAxis.setLabel(
			String.format(
				"累计上传：%s 累计下载：%s",
				FileUtils.formatSize(torrentSession.statistics().uploadSize()),
				FileUtils.formatSize(torrentSession.statistics().downloadSize())
			)
		);
		xAxis.setCategories(FXCollections.observableArrayList(categoriesPeer));
		// Y轴
		final NumberAxis yAxis = new NumberAxis();
		yAxis.setLabel("流量（MB）");
		// 流量图表
		final StackedBarChart<String, Number> stackedBarChart = new StackedBarChart<>(xAxis, yAxis);
		stackedBarChart.setPrefWidth(CHART_WIDTH);
		stackedBarChart.setPrefHeight(CHART_HEIGHT);
		stackedBarChart.setTitle("流量统计");
		// 上传流量
		final XYChart.Series<String, Number> uploadSeries = new XYChart.Series<>();
		uploadSeries.setName("上传");
		uploadSeries.getData().addAll(uploadPeer);
		// 下载流量
		final XYChart.Series<String, Number> downloadSeries = new XYChart.Series<>();
		downloadSeries.setName("下载");
		downloadSeries.getData().addAll(downloadPeer);
		// 添加数据
		stackedBarChart.getData().add(uploadSeries);
		stackedBarChart.getData().add(downloadSeries);
//		stackedBarChart.getData().addAll(List.of(uploadSeries, downloadSeries));
		// 设置提示消息
		uploadPeer.forEach(data -> {
			Tooltip.install(data.getNode(), Tooltips.newTooltip(String.format("IP：%s 上传：%.2fMB", data.getXValue(), data.getYValue())));
		});
		downloadPeer.forEach(data -> {
			Tooltip.install(data.getNode(), Tooltips.newTooltip(String.format("IP：%s 下载：%.2fMB", data.getXValue(), data.getYValue())));
		});
		this.statisticsBox.getChildren().clear();
		this.statisticsBox.getChildren().add(stackedBarChart);
	}
	
	/**
	 * <p>下载统计</p>
	 */
	private void piece() {
		final String infoHashHex = this.selectInfoHashHex();
		if(infoHashHex == null) {
			return;
		}
		final var torrentSession = TorrentManager.getInstance().torrentSession(infoHashHex);
		// 已下载Piece位图
		final var torrent = torrentSession.torrent();
		if(torrent == null) { // 磁力链接为空
			this.filter = Filter.PEER;
			this.peer();
			Alerts.info("提示消息", "磁力链接不能查看下载统计");
			return;
		}
		final int pieceSize = torrent.getInfo().pieceSize();
		final CanvasPainter painter = CanvasPainter.newInstance(12, 50, pieceSize, torrentSession.pieces(), torrentSession.selectPieces())
			.build()
			.draw();
		this.statisticsBox.getChildren().clear();
		this.statisticsBox.getChildren().add(painter.canvas());
		// 健康度
		final Text healthText = new Text("健康度：" + torrentSession.health() + "%");
		final TextFlow healthTextFlow = new TextFlow(healthText);
		final HBox healthHBox = new HBox(healthTextFlow);
		healthHBox.getStyleClass().add("health");
		this.statisticsBox.getChildren().add(healthHBox);
	}
	
	/**
	 * <p>获取选中InfoHashHex</p>
	 * 
	 * @return 选中InfoHashHex
	 */
	private String selectInfoHashHex() {
		final SelectInfoHash value = this.selectInfoHashs.getValue();
		if(value == null) {
			return null;
		}
		return value.getHash();
	}
	
	/**
	 * <p>选择BT任务事件</p>
	 */
	private EventHandler<ActionEvent> selectEvent = event -> {
		if(this.filter == Filter.PEER) {
			this.peer();
		} else if(this.filter == Filter.TRAFFIC) {
			this.traffic();
		} else if(this.filter == Filter.PIECE) {
			this.piece();
		} else {
			this.system();
		}
	};
	
	/**
	 * <p>下载任务</p>
	 */
	protected static final class SelectInfoHash {

		/**
		 * <p>任务Hash</p>
		 */
		private final String hash;
		/**
		 * <p>任务名称</p>
		 */
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
		 * <p>或者使用{@code this.selectInfoHashs.converterProperty().set}来设置</p>
		 */
		@Override
		public String toString() {
			return this.name;
		}
		
	}

}
