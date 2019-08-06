package com.acgist.snail.gui.torrent;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

import com.acgist.snail.pojo.session.TaskSession;
import com.acgist.snail.protocol.torrent.bean.TorrentFile;
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
 * BT文件选择器
 * 
 * @author acgist
 * @since 1.0.0
 */
public class TorrentFileSelecter {

	/**
	 * 下载按钮
	 */
	private Button download;
	/**
	 * 树状文件选择
	 */
	private TreeItem<HBox> root;
	/**
	 * 选择Box-文件大小
	 */
	private Map<CheckBox, Long> checkBoxSizeMap;
	/**
	 * 文件路径-选择Box
	 */
	private Map<String, CheckBox> pathCheckBoxMap;
	/**
	 * 文件路径-子树
	 */
	private Map<String, TreeItem<HBox>> pathTreeItemMap;

	/**
	 * 选择器
	 * 
	 * @param name 任务名称
	 * @param download 下载按钮
	 * @param tree 属性菜单
	 */
	private TorrentFileSelecter(String name, Button download, TreeView<HBox> tree) {
		this.checkBoxSizeMap = new HashMap<>();
		this.pathCheckBoxMap = new HashMap<>();
		this.pathTreeItemMap = new HashMap<>();
		final TreeItem<HBox> root = builcTreeItem(null, "", name, null);
		root.setExpanded(true);
		tree.setRoot(root);
		this.root = root;
		this.download = download;
	}
	
	public static final TorrentFileSelecter newInstance(String name, Button download, TreeView<HBox> tree) {
		return new TorrentFileSelecter(name, download, tree);
	}

	/**
	 * 新建树形菜单
	 * 
	 * @param path 文件路径
	 * @param size 文件大小
	 */
	public void build(String path, Long size) {
		String name = path;
		TreeItem<HBox> parent = this.root;
		if(path.contains(TorrentFile.SEPARATOR)) { // 包含路径
			String parentPath = "";
			TreeItem<HBox> treeItem = null;
			final String[] paths = path.split(TorrentFile.SEPARATOR);
			for (int index = 0; index < paths.length - 1; index++) { // 新建路径菜单
				String value = paths[index];
				parentPath += value + TorrentFile.SEPARATOR;
				treeItem = builcTreeItem(parent, parentPath, value, null);
				parent = treeItem;
			}
			name = paths[paths.length - 1];
		}
		builcTreeItem(parent, path, name, size); // 新建文件菜单
	}
	
	/**
	 * 获取选择文件的大小
	 */
	public Long size() {
		final AtomicLong totalSize = new AtomicLong(0L);
		this.pathCheckBoxMap.entrySet().stream()
			.filter(entry -> entry.getValue().isSelected())
			.map(entry -> this.checkBoxSizeMap.get(entry.getValue()))
			.filter(size -> size != null)
			.forEach(size -> totalSize.addAndGet(size));
		return totalSize.longValue();
	}
	
	/**
	 * 获取选择文件的列表
	 */
	public List<String> description() {
		return this.pathCheckBoxMap.entrySet().stream()
			.filter(entry -> entry.getValue().isSelected())
			.filter(entry -> this.checkBoxSizeMap.containsKey(entry.getValue())) // 文件而不是目录
			.map(Entry::getKey)
			.collect(Collectors.toList());
	}

	/**
	 * <p>设置已选中信息，如果没有设置将自动选择。</p>
	 * <p>自动选择：选择平均值大的文件。</p>
	 */
	public void select(TaskSession taskSession) {
		final var list = taskSession.downloadTorrentFiles();
		if(CollectionUtils.isNotEmpty(list)) { // 已选择
			this.pathCheckBoxMap.entrySet().stream()
			.filter(entry -> list.contains(entry.getKey()))
			.forEach(entry -> entry.getValue().setSelected(true));
		} else { // 自动选择
			final var avgSize = this.checkBoxSizeMap.values().stream()
				.collect(Collectors.averagingInt(Long::intValue));
			this.pathCheckBoxMap.entrySet().stream()
			.filter(entry -> {
				if(this.checkBoxSizeMap.get(entry.getValue()) == null) { // 只选择文件
					return false;
				}
				return this.checkBoxSizeMap.get(entry.getValue()) >= avgSize;
			}).forEach(entry -> entry.getValue().setSelected(true));
		}
		buttonSize();
	}
	
	/**
	 * @param parent 父节点
	 * @param path 路径
	 * @param name 名称
	 * @param size 大小
	 */
	private TreeItem<HBox> builcTreeItem(TreeItem<HBox> parent, String path, String name, Long size) {
		if(this.pathTreeItemMap.containsKey(path)) {
			return this.pathTreeItemMap.get(path);
		}
		final CheckBox checkBox = new CheckBox(name);
		checkBox.setPrefWidth(400);
		final HBox box = new HBox(checkBox);
		if(size != null) {
			final Text text = new Text(FileUtils.formatSize(size));
			this.checkBoxSizeMap.put(checkBox, size);
			box.getChildren().add(text);
		}
		checkBox.setOnAction(this.selectAction);
		final TreeItem<HBox> treeItem = new TreeItem<HBox>(box);
		this.pathCheckBoxMap.put(path, checkBox);
		this.pathTreeItemMap.put(path, treeItem);
		if(parent != null) {
			parent.getChildren().add(treeItem);
		}
		return treeItem;
	}
	
	/**
	 * 文件选择事件
	 */
	private EventHandler<ActionEvent> selectAction = (event) -> {
		final CheckBox checkBox = (CheckBox) event.getSource();
		final boolean selected = checkBox.isSelected();
		final String prefix = this.pathCheckBoxMap.entrySet().stream()
			.filter(entry -> entry.getValue() == checkBox)
			.map(entry -> entry.getKey())
			.findFirst().get();
		this.pathCheckBoxMap.entrySet().stream()
			.filter(entry -> entry.getKey().startsWith(prefix))
			.forEach(entry -> entry.getValue().setSelected(selected));
		buttonSize();
	};
	
	/**
	 * 设置按钮文本
	 */
	private void buttonSize() {
		this.download.setText("下载（" + FileUtils.formatSize(size()) + "）");
	}
	
}
