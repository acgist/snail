package com.acgist.snail.gui.javafx.window.main;

import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

import com.acgist.snail.context.GuiContext;
import com.acgist.snail.context.ProtocolContext;
import com.acgist.snail.context.StatisticsContext;
import com.acgist.snail.context.TaskContext;
import com.acgist.snail.context.exception.DownloadException;
import com.acgist.snail.gui.javafx.Alerts;
import com.acgist.snail.gui.javafx.Desktops;
import com.acgist.snail.gui.javafx.Fonts.SnailIcon;
import com.acgist.snail.gui.javafx.Themes;
import com.acgist.snail.gui.javafx.menu.TaskMenu;
import com.acgist.snail.gui.javafx.window.Controller;
import com.acgist.snail.gui.javafx.window.about.AboutWindow;
import com.acgist.snail.gui.javafx.window.build.BuildWindow;
import com.acgist.snail.gui.javafx.window.setting.SettingWindow;
import com.acgist.snail.gui.javafx.window.torrent.TorrentWindow;
import com.acgist.snail.logger.Logger;
import com.acgist.snail.logger.LoggerFactory;
import com.acgist.snail.pojo.ITaskSession;
import com.acgist.snail.protocol.Protocol;
import com.acgist.snail.protocol.Protocol.Type;
import com.acgist.snail.utils.FileUtils;
import com.acgist.snail.utils.StringUtils;

import javafx.application.Platform;
import javafx.beans.binding.DoubleBinding;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableRow;
import javafx.scene.control.TableView;
import javafx.scene.input.DragEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.util.Callback;

/**
 * <p>主窗口控制器</p>
 * 
 * @author acgist
 */
public final class MainController extends Controller {

	private static final Logger LOGGER = LoggerFactory.getLogger(MainController.class);
	
	/**
	 * <p>任务列表显示筛选</p>
	 * 
	 * @author acgist
	 */
	public enum Filter {
		
		/**
		 * <p>显示所有任务</p>
		 */
		ALL,
		/**
		 * <p>显示正在下载任务</p>
		 */
		DOWNLOAD,
		/**
		 * <p>显示下载完成任务</p>
		 */
		COMPLETED;
		
	}
	
	@FXML
	private BorderPane root;
	@FXML
	private HBox filters;
	@FXML
	private HBox statuses;
	@FXML
	private Label uploadBuffer;
	@FXML
	private Label uploadStatus;
	@FXML
	private TableView<ITaskSession> taskTable;
	@FXML
	private TableColumn<ITaskSession, String> name;
	@FXML
	private TableColumn<ITaskSession, String> status;
	@FXML
	private TableColumn<ITaskSession, String> progress;
	@FXML
	private TableColumn<ITaskSession, String> createDate;
	@FXML
	private TableColumn<ITaskSession, String> completedDate;
	/**
	 * <p>任务列表显示筛选</p>
	 */
	private Filter filter = Filter.ALL;
	
	@Override
	public void initialize(URL location, ResourceBundle resources) {
		// 设置上传图标
		this.uploadStatus.setText(SnailIcon.AS_CLOUD_UPLOAD.toString());
		this.uploadStatus.setTextFill(Themes.getColor());
		// 设置多选
		this.taskTable.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
		this.taskTable.setPlaceholder(this.buildPlaceholder());
		// 设置列
		this.taskCell(this.name, Pos.CENTER_LEFT, true, true, this.taskTable.widthProperty().multiply(3D).divide(10D));
		this.taskCell(this.status, Pos.CENTER, false, false, this.taskTable.widthProperty().multiply(1D).divide(10D));
		this.taskCell(this.progress, Pos.CENTER_LEFT, false, false, this.taskTable.widthProperty().multiply(2D).divide(10D));
		this.taskCell(this.createDate, Pos.CENTER, false, false, this.taskTable.widthProperty().multiply(2D).divide(10D));
		this.taskCell(this.completedDate, Pos.CENTER, false, false, this.taskTable.widthProperty().multiply(2D).divide(10D));
		// 设置行
		this.taskTable.setRowFactory(this.rowFactory);
		// 绑定属性
		this.taskTable.prefWidthProperty().bind(this.root.widthProperty());
		this.taskTable.prefHeightProperty().bind(this.root.prefHeightProperty().subtract(80D));
		this.filters.prefWidthProperty().bind(this.root.widthProperty().multiply(0.5D));
		this.statuses.prefWidthProperty().bind(this.root.widthProperty().multiply(0.5D));
		// 文件拖拽
		this.taskTable.setOnDragOver(this.dragOverAction);
		this.taskTable.setOnDragDropped(this.dragDroppedAction);
		// 定时刷新
		TaskDisplay.getInstance().newScheduled(this);
	}

	/**
	 * <p>新建没有下载任务时的提示信息</p>
	 * 
	 * @return 提示信息
	 */
	private Node buildPlaceholder() {
		final var color = Themes.COLOR_GRAY;
		final var icon = SnailIcon.AS_DOWNLOAD3.iconLabel();
		final var text = new Text("点击新建按钮或者拖动下载链接、种子文件开始下载");
		text.setFill(color);
		icon.setTextFill(color);
		Themes.applyClass(icon, Themes.CLASS_TASK_EMPTY);
		final var placeholder = new VBox(icon, text);
		placeholder.setAlignment(Pos.CENTER);
		return placeholder;
	}

	/**
	 * <p>新建按钮</p>
	 * 
	 * @param event 事件
	 */
	@FXML
	public void handleBuildAction(ActionEvent event) {
		BuildWindow.getInstance().show();
	}

	/**
	 * <p>开始按钮</p>
	 * 
	 * @param event 事件
	 */
	@FXML
	public void handleStartAction(ActionEvent event) {
		this.start();
	}

	/**
	 * <p>暂停按钮</p>
	 * 
	 * @param event 事件
	 */
	@FXML
	public void handlePauseAction(ActionEvent event) {
		this.pause();
	}
	
	/**
	 * <p>删除按钮</p>
	 * 
	 * @param event 事件
	 */
	@FXML
	public void handleDeleteAction(ActionEvent event) {
		this.delete();
	}
	
	/**
	 * <p>关于按钮</p>
	 * 
	 * @param event 事件
	 */
	@FXML
	public void handleAboutAction(ActionEvent event) {
		AboutWindow.getInstance().show();
	}
	
	/**
	 * <p>设置按钮</p>
	 * 
	 * @param event 事件
	 */
	@FXML
	public void handleSettingAction(ActionEvent event) {
		SettingWindow.getInstance().show();
	}
	
	/**
	 * <p>全部任务按钮</p>
	 * 
	 * @param event 事件
	 */
	@FXML
	public void handleAllAction(ActionEvent event) {
		this.filter = Filter.ALL;
		this.refreshTaskList();
	}

	/**
	 * <p>下载任务按钮</p>
	 * 
	 * @param event 事件
	 */
	@FXML
	public void handleDownloadAction(ActionEvent event) {
		this.filter = Filter.DOWNLOAD;
		this.refreshTaskList();
	}
	
	/**
	 * <p>完成任务按钮</p>
	 * 
	 * @param event 事件
	 */
	@FXML
	public void handleCompletedAction(ActionEvent event) {
		this.filter = Filter.COMPLETED;
		this.refreshTaskList();
	}
	
	/**
	 * <p>刷新任务列表</p>
	 * <p>重新设置表格数据</p>
	 */
	public void refreshTaskList() {
		final ObservableList<ITaskSession> obs = FXCollections.observableArrayList();
		TaskContext.getInstance().allTask().stream()
			.filter(session -> {
				if(this.filter == Filter.ALL) {
					return true;
				} else if(this.filter == Filter.DOWNLOAD) {
					return session.statusRunning();
				} else if(this.filter == Filter.COMPLETED) {
					return session.statusCompleted();
				} else {
					return true;
				}
			})
			.forEach(obs::add);
		this.taskTable.setItems(obs);
	}
	
	/**
	 * <p>刷新任务状态</p>
	 * <p>不设置表格数据，只刷新任务状态。</p>
	 */
	public void refreshTaskStatus() {
		// 刷新Table
		this.taskTable.refresh();
		// 上传速度
		Platform.runLater(() -> this.uploadBuffer.setText(FileUtils.formatSpeed(StatisticsContext.getInstance().uploadSpeed())));
	}
	
	/**
	 * <p>获取选中任务列表</p>
	 * 
	 * @return 选中任务列表
	 */
	public List<ITaskSession> selected() {
		return this.taskTable.getSelectionModel().getSelectedItems().stream()
			.collect(Collectors.toList());
	}
	
	/**
	 * <p>判断是否选中任务</p>
	 * 
	 * @return 是否选中任务
	 */
	public boolean hasSelected() {
		return !this.selected().isEmpty();
	}

	/**
	 * <p>获取选中BT任务列表</p>
	 * 
	 * @return 选中BT任务列表
	 */
	public List<ITaskSession> selectedTorrent() {
		return this.taskTable.getSelectionModel().getSelectedItems().stream()
			.filter(session -> session.getType() == Protocol.Type.TORRENT)
			.collect(Collectors.toList());
	}
	
	/**
	 * <p>判断是否选中BT任务</p>
	 * 
	 * @return 是否选中BT任务
	 */
	public boolean hasSelectedTorrent() {
		return !this.selectedTorrent().isEmpty();
	}

	/**
	 * <p>开始选中任务</p>
	 */
	public void start() {
		this.selected().forEach(this::start);
	}
	
	/**
	 * <p>暂停选中任务</p>
	 */
	public void pause() {
		this.selected().forEach(ITaskSession::pause);
	}
	
	/**
	 * <p>删除选中任务</p>
	 */
	public void delete() {
		if(!this.hasSelected()) {
			return;
		}
		final var optional = Alerts.build("删除确认", "删除选中任务？", GuiContext.MessageType.CONFIRM);
		if(Alerts.ok(optional)) {
			this.selected().forEach(ITaskSession::delete);
		}
	}

	/**
	 * <p>设置列</p>
	 * 
	 * @param column 列
	 * @param pos 对齐方式
	 * @param icon 是否显示Icon
	 * @param tooltip 是否显示Tooltip
	 * @param widthBinding 宽度绑定
	 */
	private void taskCell(TableColumn<ITaskSession, String> column, Pos pos, boolean icon, boolean tooltip, DoubleBinding widthBinding) {
		// 禁止修改大小
		column.setResizable(false);
		column.prefWidthProperty().bind(widthBinding);
		column.setCellFactory(tableColumn -> new TaskTableCell(pos, icon, tooltip));
	}
	
	/**
	 * <p>设置行</p>
	 */
	private Callback<TableView<ITaskSession>, TableRow<ITaskSession>> rowFactory = tableView -> {
		final TableRow<ITaskSession> row = new TableRow<>();
		// 右键菜单
		row.setContextMenu(TaskMenu.getInstance());
		// 左键双击
		row.setOnMouseClicked(this.rowClickAction);
		return row;
	};
	
	/**
	 * <p>双击事件</p>
	 */
	private EventHandler<MouseEvent> rowClickAction = event -> {
		if(event.getClickCount() == DOUBLE_CLICK_COUNT) {
			// 双击
			final var row = (TableRow<?>) event.getSource();
			final var session = (ITaskSession) row.getItem();
			if(session == null) {
				return;
			}
			if(session.statusCompleted()) {
				// 下载完成：打开任务
				if(session.getType() == Type.MAGNET) {
					// 磁力链接：转换BT任务
					TorrentWindow.getInstance().show(session);
				} else {
					// 其他：打开下载文件
					Desktops.open(session.downloadFile());
				}
			} else if(session.statusRunning()) {
				// 处于下载线程：暂停下载
				session.pause();
			} else {
				// 其他：开始下载
				this.start(session);
			}
		}
	};
	
	/**
	 * <p>开始任务</p>
	 * 
	 * @param session 任务信息
	 */
	private void start(ITaskSession session) {
		try {
			session.start();
		} catch (DownloadException e) {
			LOGGER.error("开始下载任务异常", e);
			Alerts.warn("下载失败", e.getMessage());
		}
	}
	
	/**
	 * <p>拖入文件事件（显示）</p>
	 */
	private EventHandler<DragEvent> dragOverAction = event -> {
		if (event.getGestureSource() != this.taskTable) {
			final String url = this.dragboard(event);
			if(ProtocolContext.getInstance().support(url)) {
				event.acceptTransferModes(TransferMode.COPY);
			} else {
				event.acceptTransferModes(TransferMode.NONE);
			}
		}
		event.consume();
	};
	
	/**
	 * <p>拖入文件事件（加载）</p>
	 */
	private EventHandler<DragEvent> dragDroppedAction = event -> {
		final String url = this.dragboard(event);
		if(StringUtils.isNotEmpty(url)) {
			BuildWindow.getInstance().show(url);
		}
		event.setDropCompleted(true);
		event.consume();
	};
	
}
