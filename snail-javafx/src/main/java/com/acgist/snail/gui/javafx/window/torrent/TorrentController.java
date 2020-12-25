package com.acgist.snail.gui.javafx.window.torrent;

import java.net.URL;
import java.util.ResourceBundle;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.acgist.snail.context.exception.DownloadException;
import com.acgist.snail.downloader.DownloaderManager;
import com.acgist.snail.gui.javafx.Alerts;
import com.acgist.snail.gui.javafx.Controller;
import com.acgist.snail.gui.javafx.window.main.TaskDisplay;
import com.acgist.snail.net.torrent.TorrentManager;
import com.acgist.snail.pojo.ITaskSession;
import com.acgist.snail.pojo.ITaskSession.Status;
import com.acgist.snail.pojo.bean.Torrent;
import com.acgist.snail.pojo.bean.TorrentInfo;
import com.acgist.snail.pojo.wrapper.MultifileSelectorWrapper;
import com.acgist.snail.protocol.Protocol.Type;

import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.TreeView;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

/**
 * <p>编辑任务窗口控制器</p>
 * 
 * @author acgist
 */
public final class TorrentController extends Controller implements Initializable {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(TorrentController.class);
	
	@FXML
	private FlowPane root;
	
	@FXML
	private Button download;
	@FXML
	private VBox treeBox;
	@FXML
	private VBox downloadBox;
	
	/**
	 * <p>任务信息</p>
	 */
	private ITaskSession taskSession;
	/**
	 * <p>任务文件选择器</p>
	 */
	private SelectorManager selectorManager;
	
	@Override
	public void initialize(URL location, ResourceBundle resources) {
		// 设置属性
		final double downloadBoxHeight = 40D; // 下载按钮高度
		this.downloadBox.prefWidthProperty().bind(this.root.widthProperty());
		this.downloadBox.prefHeightProperty().setValue(downloadBoxHeight);
		this.treeBox.prefWidthProperty().bind(this.root.widthProperty());
		this.treeBox.prefHeightProperty().bind(this.root.heightProperty().subtract(downloadBoxHeight));
		// 绑定事件
		this.download.setOnAction(this.downloadEvent);
	}

	/**
	 * <p>显示树形菜单</p>
	 * 
	 * @param taskSession 任务信息
	 */
	public void buildTree(ITaskSession taskSession) {
		Torrent torrent = null;
		this.taskSession = taskSession;
		final TreeView<HBox> tree = buildTree();
		try {
			torrent = TorrentManager.getInstance().newTorrentSession(taskSession.getTorrent()).torrent();
		} catch (DownloadException e) {
			LOGGER.error("种子文件解析异常", e);
			Alerts.warn("下载失败", "种子文件解析失败：" + e.getMessage());
			return;
		}
		this.selectorManager = SelectorManager.newInstance(torrent.name(), this.download, tree);
		torrent.getInfo().files().stream()
			.filter(file -> !file.path().startsWith(TorrentInfo.PADDING_FILE_PREFIX)) // 去掉填充文件
			.forEach(file -> this.selectorManager.build(file.path(), file.getLength()));
		this.selectorManager.select(taskSession);
	}
	
	/**
	 * <p>释放资源</p>
	 */
	public void release() {
		this.selectorManager = null;
		this.treeBox.getChildren().clear();
	}
	
	/**
	 * <p>创建树形菜单</p>
	 * 
	 * @return 树形菜单
	 */
	private TreeView<HBox> buildTree() {
		final TreeView<HBox> tree = new TreeView<>();
		tree.setId("tree");
		tree.prefWidthProperty().bind(this.root.widthProperty());
		tree.prefHeightProperty().bind(this.treeBox.heightProperty());
		this.treeBox.getChildren().clear();
		this.treeBox.getChildren().add(tree);
		return tree;
	}
	
	/**
	 * <p>下载按钮事件</p>
	 */
	private EventHandler<ActionEvent> downloadEvent = event -> {
		final var list = this.selectorManager.description();
		if(list.isEmpty()) {
			Alerts.warn("下载失败", "请选择下载文件");
			return;
		}
		this.taskSession.setSize(this.selectorManager.size());
		final MultifileSelectorWrapper wrapper = MultifileSelectorWrapper.newEncoder(list);
		this.taskSession.setDescription(wrapper.serialize());
		if(this.taskSession.getId() != null) { // 已经保存实体
			boolean magnetToTorrent = false;
			// 磁力链接转为BT任务
			if(this.taskSession.getType() == Type.MAGNET) {
				magnetToTorrent = true;
				this.taskSession.setType(Type.TORRENT);
				this.taskSession.setStatus(Status.AWAIT);
				this.taskSession.setEndDate(null);
			}
			// 更新任务
			this.taskSession.update();
			if(magnetToTorrent) {
				// 切换下载器并且重新下载
				try {
					DownloaderManager.getInstance().restart(this.taskSession);
				} catch (DownloadException e) {
					LOGGER.error("切换下载器异常", e);
				}
			} else {
				// 刷新任务
				DownloaderManager.getInstance().refresh(this.taskSession);
			}
		}
		TaskDisplay.getInstance().refreshTaskStatus();
		TorrentWindow.getInstance().hide();
	};
	
}
