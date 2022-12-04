package com.acgist.snail.gui.javafx.window.statistics;

import java.net.URL;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.ResourceBundle;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import com.acgist.snail.config.PeerConfig;
import com.acgist.snail.config.SystemConfig;
import com.acgist.snail.context.StatisticsContext;
import com.acgist.snail.context.SystemContext;
import com.acgist.snail.gui.javafx.Alerts;
import com.acgist.snail.gui.javafx.Themes;
import com.acgist.snail.gui.javafx.Tooltips;
import com.acgist.snail.gui.javafx.window.Controller;
import com.acgist.snail.logger.Logger;
import com.acgist.snail.logger.LoggerFactory;
import com.acgist.snail.net.NatContext;
import com.acgist.snail.net.torrent.IPeerConnect;
import com.acgist.snail.net.torrent.TorrentContext;
import com.acgist.snail.net.torrent.TorrentSession;
import com.acgist.snail.net.torrent.dht.NodeContext;
import com.acgist.snail.net.torrent.dht.NodeSession;
import com.acgist.snail.net.torrent.peer.PeerContext;
import com.acgist.snail.net.torrent.peer.PeerSession;
import com.acgist.snail.net.torrent.tracker.TrackerContext;
import com.acgist.snail.net.torrent.tracker.TrackerSession;
import com.acgist.snail.utils.FileUtils;
import com.acgist.snail.utils.NetUtils;
import com.acgist.snail.utils.StringUtils;
import com.acgist.snail.utils.ThreadUtils;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.PieChart;
import javafx.scene.chart.StackedBarChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Label;
import javafx.scene.control.Tooltip;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;

/**
 * <p>统计窗口控制器</p>
 * 
 * @author acgist
 */
public final class StatisticsController extends Controller {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(StatisticsController.class);
	
	/**
	 * <p>位图宽度：{@value}</p>
	 */
	private static final int WH = 12;
	/**
	 * <p>位图列长：{@value}</p>
	 */
	private static final int COL = 50;
	/**
	 * <p>图表宽度：{@value}</p>
	 */
	private static final int CHART_WIDTH = 800;
	/**
	 * <p>图表高度：{@value}</p>
	 */
	private static final int CHART_HEIGHT = 400;
	
	/**
	 * <p>统计筛选</p>
	 * 
	 * @author acgist
	 */
	public enum Filter {
		
		/**
		 * <p>系统信息</p>
		 */
		SYSTEM,
		/**
		 * <p>节点统计</p>
		 */
		NODE,
		/**
		 * <p>Tracker统计</p>
		 */
		TRACKER,
		/**
		 * <p>客户端统计</p>
		 */
		CLIENT,
		/**
		 * <p>来源统计</p>
		 */
		SOURCE,
		/**
		 * <p>连接统计</p>
		 */
		CONNECT,
		/**
		 * <p>流量统计</p>
		 */
		TRAFFIC,
		/**
		 * <p>Piece统计</p>
		 */
		PIECE;
		
	}
	
	@FXML
	private FlowPane root;
	@FXML
	private Text upload;
	@FXML
	private Text download;
	@FXML
	private ChoiceBox<SelectInfoHash> selectInfoHashs;
	@FXML
	private VBox statisticsBox;
	/**
	 * <p>统计筛选</p>
	 */
	private Filter filter = Filter.SYSTEM;
	/**
	 * <p>是否隐藏未知终端</p>
	 */
	private boolean hiddenUnknownClient = false;
	
	@Override
	public void initialize(URL location, ResourceBundle resources) {
		this.selectInfoHashs.setOnAction(this.selectInfoHashsEvent);
	}
	
	@Override
	public void release() {
		super.release();
		this.statisticsBoxClear();
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
	 * <p>节点统计</p>
	 * 
	 * @param event 事件
	 */
	@FXML
	public void handleNodeAction(ActionEvent event) {
		this.filter = Filter.NODE;
		this.buildSelectNodeStatistics();
	}
	
	/**
	 * <p>Tracker统计</p>
	 * 
	 * @param event 事件
	 */
	@FXML
	public void handleTrackerAction(ActionEvent event) {
		this.filter = Filter.TRACKER;
		this.buildSelectTrackerStatistics();
	}
	
	/**
	 * <p>客户端统计</p>
	 * 
	 * @param event 事件
	 */
	@FXML
	public void handleClientAction(ActionEvent event) {
		this.filter = Filter.CLIENT;
		this.buildSelectClientStatistics();
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
	 * <p>整理内存</p>
	 * 
	 * @param event 事件
	 */
	@FXML
	public void handleMemoryAction(ActionEvent event) {
		SystemContext.gc();
	}
	
	/**
	 * <p>来源统计</p>
	 * 
	 * @param event 事件
	 */
	@FXML
	public void handleSourceAction(ActionEvent event) {
		this.filter = Filter.SOURCE;
		this.buildSelectSourceStatistics();
	}
	
	/**
	 * <p>连接统计</p>
	 * 
	 * @param event 事件
	 */
	@FXML
	public void handleConnectAction(ActionEvent event) {
		this.filter = Filter.CONNECT;
		this.buildSelectConnectStatistics();
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
	 * <p>Piece统计</p>
	 * 
	 * @param event 事件
	 */
	@FXML
	public void handlePieceAction(ActionEvent event) {
		this.filter = Filter.PIECE;
		this.buildSelectPieceStatistics();
	}

	/**
	 * <p>指定下载Piece索引</p>
	 * 
	 * @param index Piece索引
	 * 
	 * @see TorrentSession#piecePos(int)
	 */
	public void piecePos(int index) {
		final String infoHashHex = this.selectInfoHashHex();
		if(infoHashHex == null) {
			return;
		}
		final TorrentSession session = TorrentContext.getInstance().torrentSession(infoHashHex);
		if(session == null) {
			return;
		}
		session.piecePos(index);
	}
	
	/**
	 * <p>统计信息</p>
	 * 
	 * @see #buildSelectInfoHashs()
	 * @see #buildSelectStatistics()
	 * @see #buildSystemTrafficStatistics()
	 */
	public void statistics() {
		this.buildSelectInfoHashs();
		this.buildSelectStatistics();
		this.buildSystemTrafficStatistics();
	}

	/**
	 * <p>设置InfoHash信息</p>
	 */
	private void buildSelectInfoHashs() {
		final var defaultValue = this.selectInfoHashs.getValue();
		final ObservableList<SelectInfoHash> obs = FXCollections.observableArrayList();
		TorrentContext.getInstance().allTorrentSession().stream()
			// 准备完成：完成磁力链接不能查询
			.filter(TorrentSession::useable)
			.forEach(session -> obs.add(new SelectInfoHash(session.infoHashHex(), session.name())));
		this.selectInfoHashs.setItems(obs);
		// 没有选中任务：默认选中第一个任务
		int index = 0;
		if(defaultValue != null) {
			// 已经选中任务
			index = obs.indexOf(defaultValue);
		}
		this.selectInfoHashs.getSelectionModel().select(index);
	}
	
	/**
	 * <p>筛选统计页面</p>
	 */
	private void buildSelectStatistics() {
		if(this.filter == Filter.NODE) {
			this.buildSelectNodeStatistics();
		} else if(this.filter == Filter.TRACKER) {
			this.buildSelectTrackerStatistics();
		} else if(this.filter == Filter.CLIENT) {
			this.buildSelectClientStatistics();
		} else if(this.filter == Filter.SOURCE) {
			this.buildSelectSourceStatistics();
		} else if(this.filter == Filter.CONNECT) {
			this.buildSelectConnectStatistics();
		} else if(this.filter == Filter.TRAFFIC) {
			this.buildSelectTrafficStatistics();
		} else if(this.filter == Filter.PIECE) {
			this.buildSelectPieceStatistics();
		} else {
			this.buildSelectSystemStatistics();
		}
	}
	
	/**
	 * <p>系统流量统计</p>
	 */
	private void buildSystemTrafficStatistics() {
		final var statistics = StatisticsContext.getInstance().statistics();
		// 累计上传
		this.upload.setText(FileUtils.formatSize(statistics.uploadSize()));
		// 累计下载
		this.download.setText(FileUtils.formatSize(statistics.downloadSize()));
	}
	
	/**
	 * <p>系统信息</p>
	 */
	private void buildSelectSystemStatistics() {
		final var runtime = Runtime.getRuntime();
		final VBox systemInfo = new VBox(
			this.buildTextFlow("本机IP：", NetUtils.LOCAL_HOST_ADDRESS),
			this.buildTextFlow("外网IP：", SystemConfig.getExternalIPAddress()),
			this.buildTextFlow("外网端口：", SystemConfig.getTorrentPortExt()),
			this.buildTextFlow("内网穿透：", NatContext.getInstance().type()),
			this.buildTextFlow("软件版本：", SystemConfig.getVersion()),
			this.buildTextFlow("线程总量：", ThreadUtils.activeCount()),
			this.buildTextFlow("内存空闲：", FileUtils.formatSize(runtime.freeMemory())),
			this.buildTextFlow("内存占用：", FileUtils.formatSize(runtime.totalMemory())),
			this.buildTextFlow("系统名称：", System.getProperty("os.name")),
			this.buildTextFlow("系统版本：", System.getProperty("os.version")),
			this.buildTextFlow("Java版本：", System.getProperty("java.version")),
			this.buildTextFlow("虚拟机名称：", System.getProperty("java.vm.name"))
		);
		Themes.applyClass(systemInfo, Themes.CLASS_SYSTEM_INFO);
		// 添加节点
		this.statisticsBoxClear().add(systemInfo);
	}
	
	/**
	 * <p>节点统计</p>
	 */
	private void buildSelectNodeStatistics() {
		final List<NodeSession> nodes = NodeContext.getInstance().nodes();
		final Map<NodeSession.Status, Long> group = nodes.stream()
			.collect(Collectors.groupingBy(NodeSession::getStatus, Collectors.counting()));
		final long unuse = group.getOrDefault(NodeSession.Status.UNUSE, 0L);
		final long verify = group.getOrDefault(NodeSession.Status.VERIFY, 0L);
		final long available = group.getOrDefault(NodeSession.Status.AVAILABLE, 0L);
		final ObservableList<PieChart.Data> pieChartData = FXCollections.observableArrayList(
			new PieChart.Data("验证", verify),
			new PieChart.Data("可用", available),
			new PieChart.Data("未知", unuse)
		);
		final String title = String.format("总量：%d", nodes.size());
		final PieChart pieChart = this.buildPieChart(title, pieChartData);
		// 添加节点
		this.statisticsBoxClear().add(pieChart);
	}
	
	/**
	 * <p>Tracker统计</p>
	 */
	private void buildSelectTrackerStatistics() {
		final List<TrackerSession> trackers = TrackerContext.getInstance().sessions();
		final Map<Boolean, Long> group = trackers.stream()
			.collect(Collectors.groupingBy(TrackerSession::available, Collectors.counting()));
		final long disable = group.getOrDefault(Boolean.FALSE, 0L);
		final long available = group.getOrDefault(Boolean.TRUE, 0L);
		final ObservableList<PieChart.Data> pieChartData = FXCollections.observableArrayList(
			new PieChart.Data("禁用", disable),
			new PieChart.Data("可用", available)
		);
		final String title = String.format("总量：%d", trackers.size());
		final PieChart pieChart = this.buildPieChart(title, pieChartData);
		// 添加节点
		this.statisticsBoxClear().add(pieChart);
	}

	/**
	 * <p>客户端统计</p>
	 */
	private void buildSelectClientStatistics() {
		final String infoHashHex = this.selectInfoHashHex();
		if(infoHashHex == null) {
			return;
		}
		final var ipv4Count = new AtomicInteger(0);
		final var ipv6Count = new AtomicInteger(0);
		final var peers = PeerContext.getInstance().listPeerSession(infoHashHex);
		peers.forEach(peer -> {
			if(NetUtils.ipv4(peer.host())) {
				ipv4Count.incrementAndGet();
			} else {
				ipv6Count.incrementAndGet();
			}
		});
		final var pieCharts = peers.stream()
			.collect(Collectors.groupingBy(PeerSession::clientName, Collectors.counting()))
			.entrySet().stream()
			// 过滤未知终端
			.filter(entry -> !(this.hiddenUnknownClient && PeerConfig.UNKNOWN.equals(entry.getKey())))
			// 排序：数量倒序
			.sorted((source, target) -> Long.compare(target.getValue(), source.getValue()))
			.map(entity -> new PieChart.Data(entity.getKey(), entity.getValue()))
			.collect(Collectors.toList());
		final ObservableList<PieChart.Data> pieChartData = FXCollections.observableArrayList(pieCharts);
		final String title = String.format("总量：%d IPv4：%d IPv6：%d", peers.size(), ipv4Count.get(), ipv6Count.get());
		final PieChart pieChart = this.buildPieChart(title, pieChartData);
		pieChart.setOnMouseClicked(this.clientClickEvent);
		// 添加节点
		this.statisticsBoxClear().add(pieChart);
	}
	
	/**
	 * <p>来源统计</p>
	 */
	private void buildSelectSourceStatistics() {
		final String infoHashHex = this.selectInfoHashHex();
		if(infoHashHex == null) {
			return;
		}
		final var peers = PeerContext.getInstance().listPeerSession(infoHashHex);
		final var availableCount = new AtomicInteger(0);
		final var pexCount = new AtomicInteger(0);
		final var dhtCount = new AtomicInteger(0);
		final var lsdCount = new AtomicInteger(0);
		final var trackerCount = new AtomicInteger(0);
		final var connectCount = new AtomicInteger(0);
		final var holepunchCount = new AtomicInteger(0);
		peers.forEach(peer -> {
			if(peer.available()) {
				availableCount.incrementAndGet();
			}
			peer.sources().forEach(source -> {
				switch (source) {
					case DHT -> dhtCount.incrementAndGet();
					case PEX -> pexCount.incrementAndGet();
					case LSD -> lsdCount.incrementAndGet();
					case TRACKER -> trackerCount.incrementAndGet();
					case CONNECT -> connectCount.incrementAndGet();
					case HOLEPUNCH -> holepunchCount.incrementAndGet();
					default -> LOGGER.warn("未知来源：{}", source);
				}
			});
		});
		final ObservableList<PieChart.Data> pieChartData = FXCollections.observableArrayList(
			new PieChart.Data("DHT", dhtCount.get()),
			new PieChart.Data("PEX", pexCount.get()),
			new PieChart.Data("LSD", lsdCount.get()),
			new PieChart.Data("Tracker", trackerCount.get()),
			new PieChart.Data("Connect", connectCount.get()),
			new PieChart.Data("Holepunch", holepunchCount.get())
		);
		final String title = String.format("总量：%d 可用数量：%d", peers.size(), availableCount.get());
		final PieChart pieChart = this.buildPieChart(title, pieChartData);
		// 添加节点
		this.statisticsBoxClear().add(pieChart);
	}
	
	/**
	 * <p>连接统计</p>
	 */
	private void buildSelectConnectStatistics() {
		final String infoHashHex = this.selectInfoHashHex();
		if(infoHashHex == null) {
			return;
		}
		final var peers = PeerContext.getInstance().listPeerSession(infoHashHex);
		// 分类：上传、下载
		final List<String> categoriesPeer = new ArrayList<>();
		final var utpCount = new AtomicInteger(0);
		final var tcpCount = new AtomicInteger(0);
		final var uploadCount = new AtomicInteger(0);
		final var downloadCount = new AtomicInteger(0);
		// 上传流量
		final List<XYChart.Data<String, Number>> uploadPeer = new ArrayList<>();
		// 下载流量
		final List<XYChart.Data<String, Number>> downloadPeer = new ArrayList<>();
		peers.forEach(peer -> {
			// 统计下载中和上传中的Peer
			boolean active = false;
			if(peer.uploading()) {
				active = true;
				uploadCount.incrementAndGet();
			}
			if(peer.downloading()) {
				active = true;
				downloadCount.incrementAndGet();
			}
			if(active) {
				final IPeerConnect.ConnectType connectType = peer.connectType();
				if(connectType == IPeerConnect.ConnectType.TCP) {
					tcpCount.incrementAndGet();
				} else if(connectType == IPeerConnect.ConnectType.UTP) {
					utpCount.incrementAndGet();
				} else {
					LOGGER.info("未知连接类型：{}", connectType);
				}
				int index = 0;
				String name = peer.host();
				while(categoriesPeer.contains(name)) {
					// 如果已经存在重新命名：由于双向连接可能出现重复
					index++;
					name = String.format("%s-%d", name, index);
				}
				categoriesPeer.add(name);
				final double uploadSize = FileUtils.formatSizeMB(peer.statistics().uploadSize());
				final double downloadSize = FileUtils.formatSizeMB(peer.statistics().downloadSize());
				final XYChart.Data<String, Number> uploadData = new XYChart.Data<>(name, uploadSize);
				final XYChart.Data<String, Number> downloadData = new XYChart.Data<>(name, downloadSize);
				uploadPeer.add(uploadData);
				downloadPeer.add(downloadData);
			}
		});
		// X轴
		final CategoryAxis xAxis = new CategoryAxis();
		final String title = String.format(
			"UTP连接：%d TCP连接：%d 上传数量：%d 下载数量：%d",
			utpCount.get(),
			tcpCount.get(),
			uploadCount.get(),
			downloadCount.get()
		);
		xAxis.setLabel(title);
		xAxis.setCategories(FXCollections.observableArrayList(categoriesPeer));
		// Y轴
		final NumberAxis yAxis = new NumberAxis();
		yAxis.setLabel("流量（MB）");
		// 流量图表
		final StackedBarChart<String, Number> stackedBarChart = new StackedBarChart<>(xAxis, yAxis);
		stackedBarChart.setTitle("连接统计");
		stackedBarChart.setPrefWidth(CHART_WIDTH);
		stackedBarChart.setPrefHeight(CHART_HEIGHT);
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
		// 设置提示消息
		uploadPeer.forEach(data -> Tooltip.install(data.getNode(), Tooltips.newTooltip(String.format("IP：%s 上传：%.2fMB", data.getXValue(), data.getYValue()))));
		downloadPeer.forEach(data -> Tooltip.install(data.getNode(), Tooltips.newTooltip(String.format("IP：%s 下载：%.2fMB", data.getXValue(), data.getYValue()))));
		// 添加节点
		this.statisticsBoxClear().add(stackedBarChart);
	}
	
	/**
	 * <p>流量统计</p>
	 */
	private void buildSelectTrafficStatistics() {
		final String infoHashHex = this.selectInfoHashHex();
		if(infoHashHex == null) {
			return;
		}
		final var peers = PeerContext.getInstance().listPeerSession(infoHashHex);
		final var torrentSession = TorrentContext.getInstance().torrentSession(infoHashHex);
		final int peerSize = peers.size();
		if(peerSize > CanvasPainter.MAX_LENGTH) {
			Alerts.warn("警告", "当前Peer数量超过最大显示数量：" + peerSize);
			return;
		}
		PeerSession peer;
		final BitSet uploadPeers = new BitSet();
		final BitSet downloadPeers = new BitSet();
		final BitSet exchangePeers = new BitSet();
		final BitSet indifferencePeers = new BitSet();
		for (int index = 0; index < peerSize; index++) {
			peer = peers.get(index);
			final long uploadSize = peer.uploadSize();
			final long downloadSize = peer.downloadSize();
			if(uploadSize != 0L && downloadSize != 0L) {
				exchangePeers.set(index);
			} else if(uploadSize > 0L) {
				uploadPeers.set(index);
			} else if(downloadSize > 0L) {
				downloadPeers.set(index);
			} else {
				indifferencePeers.set(index);
			}
		}
		final Color[] colors = new Color[] { Themes.COLOR_YELLOW, Themes.COLOR_RED, Themes.COLOR_GREEN, Themes.COLOR_GRAY };
		final CanvasPainter painter = CanvasPainter.newInstance(
			WH, COL, peerSize,
			new BitSet[] { exchangePeers, uploadPeers, downloadPeers, indifferencePeers },
			colors
		)
			.build()
			.draw();
		final String message = String.format(
			"累计上传：%s 累计下载：%s",
			FileUtils.formatSize(torrentSession.statistics().uploadSize()),
			FileUtils.formatSize(torrentSession.statistics().downloadSize())
		);
		final HBox trafficBox = this.buildStatisticsInfo(message);
		final String[] tabs = new String[] { "交战", "上传", "下载", "无情" };
		final HBox painterBox = this.buildPainterInfo(tabs, colors);
		// 添加节点
		final var statisticsBoxNode = this.statisticsBoxClear();
		statisticsBoxNode.add(trafficBox);
		statisticsBoxNode.add(painter.canvas());
		statisticsBoxNode.add(painterBox);
	}
	
	/**
	 * <p>Piece统计</p>
	 */
	private void buildSelectPieceStatistics() {
		final String infoHashHex = this.selectInfoHashHex();
		if(infoHashHex == null) {
			return;
		}
		final var torrentSession = TorrentContext.getInstance().torrentSession(infoHashHex);
		final var torrent = torrentSession.torrent();
		if(torrent == null) {
			// 磁力链接
			this.filter = Filter.SYSTEM;
			this.buildSelectSystemStatistics();
			return;
		}
		final int pieceSize = torrent.getInfo().pieceSize();
		if(pieceSize > CanvasPainter.MAX_LENGTH) {
			Alerts.warn("警告", "当前Piece数量超过最大显示数量：" + pieceSize);
			return;
		}
		// 已经下载Pieces
		final BitSet pieces = torrentSession.pieces();
		// 选择下载Pieces
		final BitSet selectPieces = torrentSession.selectPieces();
		// 鼠标可选Pieces
		final BitSet mousePieces = new BitSet();
		mousePieces.or(selectPieces);
		mousePieces.andNot(pieces);
		final Color[] colors = new Color[] { Themes.COLOR_GREEN, Themes.COLOR_YELLOW };
		final CanvasPainter painter = CanvasPainter.newInstance(
			WH, COL, pieceSize,
			new BitSet[] { pieces, selectPieces },
			colors,
			mousePieces, this::piecePos
		)
			.build()
			.draw();
		final HBox healthBox = this.buildStatisticsInfo("健康度：" + torrentSession.health() + "%");
		final String[] tabs = new String[] { "已下载", "未下载", "不下载" };
		final Color[] tabColors = new Color[] { Themes.COLOR_GREEN, Themes.COLOR_YELLOW, Themes.COLOR_GRAY };
		final HBox painterBox = this.buildPainterInfo(tabs, tabColors);
		// 添加节点
		final var statisticsBoxNode = this.statisticsBoxClear();
		statisticsBoxNode.add(healthBox);
		statisticsBoxNode.add(painter.canvas());
		statisticsBoxNode.add(painterBox);
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
	 * <p>新建系统信息节点</p>
	 * 
	 * @param name 名称
	 * @param info 信息
	 * 
	 * @return 节点
	 */
	private TextFlow buildTextFlow(String name, Object info) {
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
	 * <p>新建统计信息节点</p>
	 * 
	 * @param message 统计信息
	 * 
	 * @return 节点
	 */
	private HBox buildStatisticsInfo(String message) {
		final Text text = new Text(message);
		final TextFlow textFlow = new TextFlow(text);
		final HBox box = new HBox(textFlow);
		Themes.applyClass(box, Themes.CLASS_STATISTICS_INFO);
		return box;
	}
	
	/**
	 * <p>新建画图颜色描述节点</p>
	 * 
	 * @param tabs 颜色描述
	 * @param colors 颜色
	 * 
	 * @return 节点
	 */
	private HBox buildPainterInfo(String [] tabs, Color [] colors) {
		final HBox box = new HBox();
		for (int index = 0; index < tabs.length; index++) {
			final Label label = new Label(tabs[index]);
			final TextFlow textFlow = new TextFlow(label);
			textFlow.setBackground(new Background(new BackgroundFill(colors[index], null, null)));
			box.getChildren().add(textFlow);
		}
		Themes.applyClass(box, Themes.CLASS_PAINTER_INFO);
		return box;
	}
	
	/**
	 * <p>新建饼状图</p>
	 * 
	 * @param title 标题
	 * @param pieChartData 饼状图数据
	 * 
	 * @return 饼状图
	 */
	private PieChart buildPieChart(String title, ObservableList<PieChart.Data> pieChartData) {
		final PieChart pieChart = new PieChart(pieChartData);
		pieChart.setTitle(title);
		pieChart.setPrefWidth(CHART_WIDTH);
		pieChart.setPrefHeight(CHART_HEIGHT);
		pieChartData.forEach(data -> Tooltip.install(data.getNode(), Tooltips.newTooltip(String.format("%s：%.0f", data.getName(), data.getPieValue()))));
		return pieChart;
	}
	
	/**
	 * <p>获取统计节点并清空</p>
	 * 
	 * @return 统计节点
	 */
	private ObservableList<Node> statisticsBoxClear() {
		final var nodes = this.statisticsBox.getChildren();
		nodes.clear();
		return nodes;
	}
	
	/**
	 * <p>选择BT任务事件</p>
	 */
	private EventHandler<ActionEvent> selectInfoHashsEvent = event -> this.buildSelectStatistics();
	
	/**
	 * <p>客户端统计点击事件</p>
	 */
	private EventHandler<MouseEvent> clientClickEvent = event -> {
		this.hiddenUnknownClient = !this.hiddenUnknownClient;
		this.buildSelectClientStatistics();
	};
	
	/**
	 * <p>BT任务InfoHash</p>
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
			return Objects.hash(this.hash);
		}
		
		@Override
		public boolean equals(Object object) {
			if(this == object) {
				return true;
			}
			if(object instanceof SelectInfoHash selectInfoHash) {
				return StringUtils.equals(this.hash, selectInfoHash.hash);
			}
			return false;
		}
		
		@Override
		public String toString() {
			// 设置下拉框显示名称
			return this.name;
		}
		
	}

}
