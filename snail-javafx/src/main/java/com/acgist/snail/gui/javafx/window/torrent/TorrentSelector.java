package com.acgist.snail.gui.javafx.window.torrent;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.acgist.snail.context.ITaskSession;
import com.acgist.snail.gui.javafx.Tooltips;
import com.acgist.snail.net.torrent.TorrentFile;
import com.acgist.snail.utils.CollectionUtils;
import com.acgist.snail.utils.FileUtils;

import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import javafx.scene.layout.HBox;
import javafx.scene.text.Text;

/**
 * BT任务下载文件选择器
 * 
 * @author acgist
 */
public final class TorrentSelector {
    
    /**
     * 根节点路径
     */
    private static final String ROOT_PATH = "";

    /**
     * 下载按钮
     */
    private final Button download;
    /**
     * 树形菜单根节点
     */
    private final TreeItem<HBox> root;
    /**
     * 文件选择器Map
     * 文件路径=文件选择器
     */
    private final Map<String, Selector> selectorMap = new HashMap<>();

    /**
     * @param name     任务名称
     * @param download 下载按钮
     * @param tree     树形菜单
     */
    private TorrentSelector(String name, Button download, TreeView<HBox> tree) {
        this.root = this.buildPath(null, ROOT_PATH, name);
        this.root.setExpanded(true);
        tree.setRoot(this.root);
        this.download = download;
    }
    
    /**
     * @param name     任务名称
     * @param download 下载按钮
     * @param tree     树形菜单
     * 
     * @return {@link TorrentSelector}
     */
    public static final TorrentSelector newInstance(String name, Button download, TreeView<HBox> tree) {
        return new TorrentSelector(name, download, tree);
    }

    /**
     * 新建文件菜单
     * 
     * @param torrentFile 文件
     */
    public void build(TorrentFile torrentFile) {
        final String path     = torrentFile.path();
        final Long   size     = torrentFile.getLength();
        String fileName       = path;
        TreeItem<HBox> parent = this.root;
        // 逐级新建路径菜单
        if(path.contains(TorrentFile.SEPARATOR)) {
            final StringBuilder parentPath = new StringBuilder(ROOT_PATH);
            final String[] paths = path.split(TorrentFile.SEPARATOR);
            // 数量大时forEach效率很低
            for (int index = 0; index < paths.length - 1; index++) {
                final String value = paths[index];
                parentPath.append(value).append(TorrentFile.SEPARATOR);
                parent = this.buildPath(parent, parentPath.toString(), value);
            }
            fileName = paths[paths.length - 1];
        }
        // 新建文件菜单
        this.buildFile(parent, path, fileName, size);
    }
    
    /**
     * 新建路径菜单
     * 
     * @param parent 上级节点
     * @param path   路径
     * @param name   名称
     * 
     * @return 路径菜单
     * 
     * @see #buildFile(TreeItem, String, String, Long)
     */
    private TreeItem<HBox> buildPath(TreeItem<HBox> parent, String path, String name) {
        return this.buildFile(parent, path, name, null);
    }
    
    /**
     * 新建文件菜单
     * 
     * @param parent 上级节点
     * @param path   路径
     * @param name   名称
     * @param size   大小
     * 
     * @return 文件菜单
     */
    private TreeItem<HBox> buildFile(TreeItem<HBox> parent, String path, String name, Long size) {
        final Selector oldSelector = this.selectorMap.get(path);
        if(oldSelector != null) {
            // 如果已经新建跳过（路径菜单可能重复）
            return oldSelector.getTreeItem();
        }
        final CheckBox nameCheckBox = new CheckBox(name);
        nameCheckBox.setPrefWidth(500);
        nameCheckBox.setTooltip(Tooltips.newTooltip(name));
        nameCheckBox.setOnAction(this.selectAction);
        final HBox box = new HBox(nameCheckBox);
        // 设置文件大小
        if(size != null) {
            final Text sizeText = new Text(FileUtils.formatSize(size));
            box.getChildren().add(sizeText);
        }
        final TreeItem<HBox> treeItem = new TreeItem<>(box);
        this.selectorMap.put(path, new Selector(path, size, nameCheckBox, treeItem));
        if(parent != null) {
            // 根节点没有上级节点
            parent.getChildren().add(treeItem);
        }
        return treeItem;
    }
    
    /**
     * @return 选择文件流
     */
    private Stream<Selector> selectedFileSelectors() {
        return this.selectorMap.values().stream()
            .filter(Selector::isSelected)
            .filter(Selector::isFile);
    }
    
    /**
     * @return 选择文件大小
     */
    public Long selectedFileSize() {
        return this.selectedFileSelectors()
            .collect(Collectors.summingLong(Selector::getSize));
    }
    
    /**
     * @return 选择文件列表
     */
    public List<String> selectedFilePath() {
        return this.selectedFileSelectors()
            .map(Selector::getPath)
            .collect(Collectors.toList());
    }

    /**
     * 设置选择文件
     * 如果没有选择文件使用自动选择（选择大于平均值的文件）
     * 
     * @param taskSession 任务信息
     */
    public void select(ITaskSession taskSession) {
        final List<String> selectedFileList = taskSession.multifileSelected();
        if(CollectionUtils.isNotEmpty(selectedFileList)) {
            // 已选择文件
            this.selectorMap.values().stream()
                .filter(selector -> selectedFileList.contains(selector.getPath()))
                .forEach(Selector::setSelected);
        } else {
            // 未选择文件
            // 计算平均值
            final Double avgSize = this.selectorMap.values().stream()
                .collect(Collectors.averagingLong(Selector::getSize));
            // 自动选择大于平均值的文件
            this.selectorMap.values().stream()
                .filter(Selector::isFile)
                .filter(selector -> selector.getSize() >= avgSize)
                .forEach(Selector::setSelected);
        }
        this.selectParentFolder();
    }
    
    /**
     * 选择上级目录
     * 选中文件时同时选中所有上级目录
     */
    private void selectParentFolder() {
        final List<TreeItem<HBox>> parents = new ArrayList<>();
        // 所有上级目录
        this.selectedFileSelectors().forEach(selector -> {
            TreeItem<HBox> parent = selector.getTreeItem();
            while(parent.getParent() != null) {
                parent = parent.getParent();
                parents.add(parent);
            }
        });
        // 选择上级目录
        this.selectorMap.values().stream()
            .filter(selector -> parents.contains(selector.getTreeItem()))
            .forEach(Selector::setSelected);
        // 设置下载按钮
        this.download.setText("下载（" + FileUtils.formatSize(this.selectedFileSize()) + "）");
    }
    
    /**
     * 选择框事件
     */
    private EventHandler<ActionEvent> selectAction = event -> {
        final CheckBox checkBox = (CheckBox) event.getSource();
        final boolean selected  = checkBox.isSelected();
        // 选择文件前缀
        final String prefix = this.selectorMap.values().stream()
            .filter(selector -> selector.getCheckBox() == checkBox)
            .map(Selector::getPath)
            .findFirst()
            .orElse(ROOT_PATH);
        // 选择下级目录
        this.selectorMap.values().stream()
            .filter(selector  -> selector.getPath().startsWith(prefix))
            .forEach(selector -> selector.setSelected(selected));
        this.selectParentFolder();
    };
    
    /**
     * 文件选择器
     * 
     * @author acgist
     */
    protected static final class Selector {
        
        /**
         * 文件路径
         */
        private final String path;
        /**
         * 文件大小
         */
        private final long size;
        /**
         * 是否是文件
         */
        private final boolean file;
        /**
         * 文件选择框
         */
        private final CheckBox checkBox;
        /**
         * 文件菜单节点
         */
        private final TreeItem<HBox> treeItem;
        
        /**
         * @param path     文件路径
         * @param size     文件大小
         * @param checkBox 选择框
         * @param treeItem 文件菜单节点
         */
        protected Selector(String path, Long size, CheckBox checkBox, TreeItem<HBox> treeItem) {
            this.path = path;
            this.size = size == null ? 0L : size;
            this.file = size != null;
            this.checkBox = checkBox;
            this.treeItem = treeItem;
        }
        
        /**
         * @return 是否选中
         */
        public boolean isSelected() {
            return this.checkBox.isSelected();
        }

        /**
         * 设置选中
         */
        public void setSelected() {
            this.setSelected(true);
        }
        
        /**
         * 设置是否选中
         * 
         * @param selected 是否选中
         */
        public void setSelected(boolean selected) {
            this.checkBox.setSelected(selected);
            // 自动展开选中路径
            if(selected && !this.file) {
                this.treeItem.setExpanded(true);
            }
        }
        
        /**
         * @return 文件路径
         */
        public String getPath() {
            return this.path;
        }
        
        /**
         * @return 文件大小
         */
        public long getSize() {
            return this.size;
        }
        
        /**
         * @return 是否是文件
         */
        public boolean isFile() {
            return this.file;
        }
        
        /**
         * @return 选择框
         */
        public CheckBox getCheckBox() {
            return this.checkBox;
        }
        
        /**
         * @return 文件菜单节点
         */
        public TreeItem<HBox> getTreeItem() {
            return this.treeItem;
        }
        
    }
    
}
