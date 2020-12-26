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
import com.acgist.snail.gui.javafx.Controller;
import com.acgist.snail.gui.javafx.Tooltips;
import com.acgist.snail.net.torrent.TorrentManager;
import com.acgist.snail.net.torrent.dht.NodeManager;
import com.acgist.snail.net.torrent.peer.PeerManager;
import com.acgist.snail.net.torrent.tracker.TrackerManager;
import com.acgist.snail.pojo.session.NodeSession;
import com.acgist.snail.pojo.session.TrackerSession;
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
	 * 
	 * @author acgist
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
		this.selectInfoHashs.setOnAction(this.selectInfoHashsEvent);
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
		this.buildSelectSystemStatistics();
	}
	
	/**
	 * <p>Peer统计</p>
	 * 
	 * @param event 事件
	 */
	@FXML
	public void handlePeerAction(ActionEvent event) {
		this.filter = Filter.PEER;
		this.buildSelectPeerStatistics();
	}
	
	/**
	 * <p>流量统计</p>
	 * 
	 * @param event 事件
	 */
	@FXML
	public void handleTrafficAction(ActionEvent event) {
		this.filter = Filter.TRAFFIC;
		this.buildSelectTrafficStatistics();
	}
	
	/**
	 * <p>下载统计</p>
	 * 
	 * @param event 事件
	 */
	@FXML
	public void handlePieceAction(ActionEvent event) {
		this.filter = Filter.PIECE;
		this.buildSelectPieceStatistics();
	}
	
	/**
	 * <p>统计信息</p>
	 * 
	 * @see #buildSystemStatistics()
	 * @see #buildDhtStatistics()
	 * @see #buildTrackerStatistics()
	 * @see #buildInfoHashStatistics()
	 * @see #buildSelectStatistics()
	 */
	public void statistics() {
		this.buildSystemStatistics();
		this.buildDhtStatistics();
		this.buildTrackerStatistics();
		this.buildInfoHashStatistics();
		this.buildSelectStatistics();
	}
	
	/**
	 * <p>释放资源</p>
	 */
	public void release() {
		this.statisticsBox.getChildren().clear();
	}

	/**
	 * <p>创建统计信息</p>
	 */
	private void buildSelectStatistics() {
		if(this.filter == Filter.PEER) {
			this.buildSelectPeerStatistics();
		} else if(this.filter == Filter.TRAFFIC) {
			this.buildSelectTrafficStatistics();
		} else if(this.filter == Filter.PIECE) {
			this.buildSelectPieceStatistics();
		} else {
			this.buildSelectSystemStatistics();
		}
	}
	
	/**
	 * <p>系统统计信息</p>
	 */
	private void buildSystemStatistics() {
		final var statistics = SystemStatistics.getInstance().statistics();
		// 累计上传
		this.upload.setText(FileUtils.formatSize(statistics.uploadSize()));
		// 累计下载
		this.download.setText(FileUtils.formatSize(statistics.downloadSize()));
	}
	
	/**
	 * <p>DHT统计信息</p>
	 */
	private void buildDhtStatistics() {
		final List<NodeSession> nodes = NodeManager.getInstance().nodes();
		final Map<NodeSession.Status, Long> nodeGroup = nodes.stream()
			.collect(Collectors.groupingBy(NodeSession::getStatus, Collectors.counting()));
		this.dhtTotal.setText(String.valueOf(nodes.size()));
		this.dhtUnuse.setText(String.valueOf(nodeGroup.getOrDefault(NodeSession.Status.UNUSE, 0L)));
		this.dhtVerify.setText(String.valueOf(nodeGroup.getOrDefault(NodeSession.Status.VERIFY, 0L)));
		this.dhtAvailable.setText(String.valueOf(nodeGroup.getOrDefault(NodeSession.Status.AVAILABLE, 0L)));
	}
	
	/**
	 * <p>Tracker统计信息</p>
	 */
	private void buildTrackerStatistics() {
		final List<TrackerSession> sessions = TrackerManager.getInstance().sessions();
		final Map<Boolean, Long> clientGroup = sessions.stream()
			.collect(Collectors.groupingBy(TrackerSession::available, Collectors.counting()));
		this.trackerTotal.setText(String.valueOf(sessions.size()));
		this.trackerDisable.setText(String.valueOf(clientGroup.getOrDefault(Boolean.FALSE, 0L)));
		this.trackerAvailable.setText(String.valueOf(clientGroup.getOrDefault(Boolean.TRUE, 0L)));
	}
	
	/**
	 * <p>InfoHash统计信息</p>
	 */
	private void buildInfoHashStatistics() {
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
	 * <p>选择系统统计信息</p>
	 */
	private void buildSelectSystemStatistics() {
		final VBox systemInfo = new VBox(
			this.buildSystemText("本机IP", NetUtils.LOCAL_HOST_ADDRESS),
			this.buildSystemText("外网IP", SystemConfig.getExternalIpAddress()),
			this.buildSystemText("外网端口", SystemConfig.getTorrentPortExt()),
			this.buildSystemText("内网穿透", NatContext.getInstance().type()),
			this.buildSystemText("软件版本", SystemConfig.getVersion()),
			this.buildSystemText("系统名称", System.getProperty("os.name")),
			this.buildSystemText("系统版本", System.getProperty("os.version")),
			this.buildSystemText("Java版本", System.getProperty("java.version")),
			this.buildSystemText("虚拟机名称", System.getProperty("java.vm.name"))
		);
		systemInfo.getStyleClass().add("system-info");
		this.statisticsBox.getChildren().clear();
		this.statisticsBox.getChildren().add(systemInfo);
	}
	
	/**
	 * <p>创建系统统计节点</p>
	 * 
	 * @param name 名称
	 * @param info 信息
	 * 
	 * @return 节点
	 */
	private TextFlow buildSystemText(String name, Object info) {
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
	 * <p>选择Peer统计信息</p>
	 */
	private void buildSelectPeerStatistics() {
		final String infoHashHex = this.selectInfoHashHex();
		if(infoHashHex == null) {
			return;
		}
		final var peers = PeerManager.getInstance().listPeerSession(infoHashHex);
		// Peer
		final var utpCount = new AtomicInteger(0);
		final var availableCount = new AtomicInteger(0);
		// 上传数量、下载数量
		final var uploadCount = new AtomicInteger(0);
		final var downloadCount = new AtomicInteger(0);
		// 来源数量
		final var pexCount = new AtomicInteger(0);
		final var dhtCount = new AtomicInteger(0);
		final var lsdCount = new AtomicInteger(0);
		final var trackerCount = new AtomicInteger(0);
		final var connectCount = new AtomicInteger(0);
		final var holepunchCount = new AtomicInteger(0);
		peers.forEach(peer -> {
			if(peer.utp()) {
				utpCount.incrementAndGet();
			}
			if(peer.available()) {
				availableCount.incrementAndGet();
			}
			if(peer.uploading()) {
				uploadCount.incrementAndGet();
			}
			if(peer.downloading()) {
				downloadCount.incrementAndGet();
			}
			peer.sources().forEach(source -> {
				switch (source) {
				case DHT:
					dhtCount.incrementAndGet();
					break;
				case PEX:
					pexCount.incrementAndGet();
					break;
				case LSD:
					lsdCount.incrementAndGet();
					break;
				case TRACKER:
					trackerCount.incrementAndGet();
					break;
				case CONNECT:
					connectCount.incrementAndGet();
					break;
				case HOLEPUNCH:
					holepunchCount.incrementAndGet();
					break;
				default:
					LOGGER.warn("未知Peer来源：{}", source);
					break;
				}
			});
		});
		// 来源图表
		final ObservableList<PieChart.Data> pieChartData = FXCollections.observableArrayList(
			new PieChart.Data("DHT", dhtCount.get()),
			new PieChart.Data("PEX", pexCount.get()),
			new PieChart.Data("LSD", lsdCount.get()),
			new PieChart.Data("Tracker", trackerCount.get()),
			new PieChart.Data("Connect", connectCount.get()),
			new PieChart.Data("Holepunch", holepunchCount.get())
		);
		final PieChart pieChart = new PieChart(pieChartData);
		pieChart.setTitle(
			String.format(
				"总量：%d 可用：%d 下载：%d 上传：%d",
				peers.size(), availableCount.get(), downloadCount.get(), uploadCount.get()
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
			peers.size(), availableCount.get(), downloadCount.get(), uploadCount.get()
		);
		LOGGER.debug(
			"Peer来源，DHT：{}、PEX：{}、LSD：{}、Tracker：{}、Connect：{}、Holepunch：{}",
			dhtCount.get(), pexCount.get(), lsdCount.get(), trackerCount.get(), connectCount.get(), holepunchCount.get()
		);
	}
	
	/**
	 * <p>选择流量统计信息</p>
	 */
	private void buildSelectTrafficStatistics() {
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
	 * <p>选择下载统计信息</p>
	 */
	private void buildSelectPieceStatistics() {
		final String infoHashHex = this.selectInfoHashHex();
		if(infoHashHex == null) {
			return;
		}
		final var torrentSession = TorrentManager.getInstance().torrentSession(infoHashHex);
		// 已下载Piece位图
		final var torrent = torrentSession.torrent();
		if(torrent == null) { // 磁力链接为空
			this.filter = Filter.SYSTEM;
			this.buildSelectSystemStatistics();
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
	private EventHandler<ActionEvent> selectInfoHashsEvent = event -> {
		this.buildSelectStatistics();
	};
	
	/**
	 * <p>下载任务</p>
	 * 
	 * @author acgist
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

		/**
		 * @param hash 任务Hash
		 * @param name 任务名称
		 */
		public SelectInfoHash(String hash, String name) {
			this.hash = hash;
			this.name = name;
		}

		/**
		 * <p>获取任务Hash</p>
		 * 
		 * @return 任务Hash
		 */
		public String getHash() {
			return hash;
		}

		/**
		 * <p>获取任务名称</p>
		 * 
		 * @return 任务名称
		 */
		public String getName() {
			return name;
		}
		
		@Override
		public int hashCode() {
			return ObjectUtils.hashCode(this.hash);
		}
		
		@Override
		public boolean equals(Object object) {
			if(this == object) {
				return true;
			}
			if(object instanceof SelectInfoHash) {
				// TODO：新版强转写法
				final SelectInfoHash selectInfoHash = (SelectInfoHash) object;
				return StringUtils.equals(this.hash, selectInfoHash.hash);
			}
			return false;
		}
		
		/**
		 * <p>重写toString设置下拉框显示名称或者使用{@code this.selectInfoHashs.converterProperty().set}来设置</p>
		 */
		@Override
		public String toString() {
			return this.name;
		}
		
	}

}
