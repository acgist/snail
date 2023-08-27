package com.acgist.snail.gui.javafx.window.torrent;

import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;

import com.acgist.snail.context.ITaskSession;
import com.acgist.snail.context.wrapper.DescriptionWrapper;
import com.acgist.snail.gui.javafx.Alerts;
import com.acgist.snail.gui.javafx.window.Controller;
import com.acgist.snail.gui.javafx.window.main.TaskDisplay;
import com.acgist.snail.logger.Logger;
import com.acgist.snail.logger.LoggerFactory;
import com.acgist.snail.net.DownloadException;
import com.acgist.snail.net.torrent.Torrent;
import com.acgist.snail.net.torrent.TorrentContext;
import com.acgist.snail.net.torrent.TorrentFile;
import com.acgist.snail.net.torrent.TorrentInfo;
import com.acgist.snail.net.torrent.TorrentSession;
import com.acgist.snail.protocol.Protocol.Type;

import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TreeView;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

/**
 * 编辑任务窗口控制器
 * 
 * @author acgist
 */
public final class TorrentController extends Controller {
    
    private static final Logger LOGGER = LoggerFactory.getLogger(TorrentController.class);
    
    /**
     * 下载按钮高度：{@value}
     */
    private static final double DOWNLOAD_BUTTON_HEIGHT = 40D;
    
    @FXML
    private FlowPane root;
    @FXML
    private VBox treeBox;
    @FXML
    private VBox downloadBox;
    @FXML
    private Button download;
    
    /**
     * 任务信息
     */
    private ITaskSession taskSession;
    /**
     * BT任务下载文件选择器
     */
    private TorrentSelector torrentSelector;
    
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // 设置属性
        this.downloadBox.prefWidthProperty().bind(this.root.widthProperty());
        this.downloadBox.prefHeightProperty().setValue(DOWNLOAD_BUTTON_HEIGHT);
        this.treeBox.prefWidthProperty().bind(this.root.widthProperty());
        this.treeBox.prefHeightProperty().bind(this.root.heightProperty().subtract(DOWNLOAD_BUTTON_HEIGHT));
        // 绑定事件
        this.download.setOnAction(this.downloadEvent);
    }
    
    @Override
    public void release() {
        super.release();
        this.taskSession     = null;
        this.torrentSelector = null;
        this.treeBox.getChildren().clear();
    }

    /**
     * 新建文件选择树形菜单
     * 
     * @param taskSession 任务信息
     */
    public void buildTree(ITaskSession taskSession) {
        Torrent torrent           = null;
        final TreeView<HBox> tree = this.buildTree();
        try {
            final TorrentSession torrentSession = TorrentContext.getInstance().newTorrentSession(taskSession.getTorrent());
            torrent = torrentSession.torrent();
        } catch (DownloadException e) {
            LOGGER.error("种子文件解析异常", e);
            Alerts.warn("下载失败", e.getMessage());
        }
        if(torrent != null) {
            this.taskSession     = taskSession;
            this.torrentSelector = TorrentSelector.newInstance(torrent.name(), this.download, tree);
            final TorrentInfo torrentInfo = torrent.getInfo();
            final List<TorrentFile> files = torrentInfo.files();
            files.stream()
                .filter(TorrentFile::notPaddingFile)
                .forEach(this.torrentSelector::build);
            this.torrentSelector.select(taskSession);
        }
    }
    
    /**
     * 新建树形菜单
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
     * 下载按钮事件
     */
    private EventHandler<ActionEvent> downloadEvent = event -> {
        final List<String> list = this.torrentSelector.selectedFilePath();
        if(list.isEmpty()) {
            Alerts.warn("下载失败", "请选择下载文件");
            return;
        }
        this.taskSession.setSize(this.torrentSelector.selectedFileSize());
        this.taskSession.setDescription(DescriptionWrapper.newEncoder(list).serialize());
        if(this.taskSession.getId() != null) {
            // 已经保存实体：修改任务
            try {
                this.updateTaskSession();
            } catch (DownloadException e) {
                LOGGER.error("更新下载任务异常", e);
                Alerts.warn("下载失败", e.getMessage());
            }
        }
        // 其他情况：新建任务自动刷新列表
        TorrentWindow.getInstance().hide();
    };
    
    /**
     * 更新下载任务
     * 
     * @throws DownloadException 下载异常
     */
    private void updateTaskSession() throws DownloadException {
        final boolean magnetToTorrent = this.taskSession.getType() == Type.MAGNET;
        if(magnetToTorrent) {
            // 磁力链接任务转为BT任务
            this.taskSession.magnetToTorrent();
            // 切换下载器并且重新下载
            this.taskSession.restart();
        } else {
            // 刷新任务
            this.taskSession.refresh();
        }
        // 更新任务
        this.taskSession.update();
        // 刷新任务状态
        TaskDisplay.getInstance().refreshTaskStatus();
    }
    
}
