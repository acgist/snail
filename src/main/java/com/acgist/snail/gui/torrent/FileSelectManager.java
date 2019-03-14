package com.acgist.snail.gui.torrent;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

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
 * 路径选择器
 */
public class FileSelectManager {

	private Button download;
	private TreeItem<HBox> root;
	private Map<CheckBox, Long> sizeMap;
	private Map<String, CheckBox> checkBoxMap;
	private Map<String, TreeItem<HBox>> treeItemMap;

	/**
	 * 选择器
	 * @param name 任务名称
	 * @param download 下载按钮
	 * @param tree 属性菜单
	 */
	public FileSelectManager(String name, Button download, TreeView<HBox> tree) {
		this.sizeMap = new HashMap<>();
		this.checkBoxMap = new HashMap<>();
		this.treeItemMap = new HashMap<>();
		TreeItem<HBox> root = builcTreeItem(null, "", name, null);
		root.setExpanded(true);
		tree.setRoot(root);
		this.root = root;
		this.download = download;
	}

	/**
	 * 新建树形菜单
	 * @param path 文件路径
	 * @param size 文件大小
	 */
	public void build(String path, Long size) {
		String name = path;
		TreeItem<HBox> parent = root;
		if(path.contains("/")) {
			String[] paths = path.split("/");
			String parentPath = "";
			TreeItem<HBox> treeItem = null;
			for (int index = 0; index < paths.length - 1; index++) { // 新建路径菜单
				String value = paths[index];
				parentPath += value + "/";
				treeItem = builcTreeItem(parent, parentPath, value, null);
				parent = treeItem;
			}
			name = paths[paths.length - 1];
		}
		builcTreeItem(parent, path, name, size); // 新建文件菜单
	}
	
	/**
	 * 选择文件的大小
	 */
	public Long size() {
		AtomicLong totalSize = new AtomicLong(0L);
		checkBoxMap.entrySet()
		.stream()
		.filter(entry -> entry.getValue().isSelected())
		.map(entry -> sizeMap.get(entry.getValue()))
		.filter(size -> size != null)
		.forEach(size -> totalSize.addAndGet(size));
		return totalSize.longValue();
	}
	
	/**
	 * 选择文件的列表
	 */
	public List<String> description() {
		return checkBoxMap.entrySet()
		.stream()
		.filter(entry -> sizeMap.containsKey(entry.getValue()))
		.filter(entry -> entry.getValue().isSelected())
		.map(Entry::getKey)
		.collect(Collectors.toList());
	}

	/**
	 * 设置已选中信息
	 */
	public void select(List<String> list) {
		if(CollectionUtils.isNotEmpty(list)) {
			checkBoxMap.entrySet()
			.stream()
			.filter(entry -> list.contains(entry.getKey()))
			.forEach(entry -> entry.getValue().setSelected(true));
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
		if(treeItemMap.containsKey(path)) {
			return treeItemMap.get(path);
		}
		CheckBox checkBox = new CheckBox(name);
		checkBox.setPrefWidth(400);
		HBox box = new HBox(checkBox);
		if(size != null) {
			Text text = new Text(FileUtils.formatSize(size));
			sizeMap.put(checkBox, size);
			box.getChildren().add(text);
		}
		checkBox.setOnAction(selectAction);
		TreeItem<HBox> treeItem = new TreeItem<HBox>(box);
		checkBoxMap.put(path, checkBox);
		treeItemMap.put(path, treeItem);
		if(parent != null) {
			parent.getChildren().add(treeItem);
		}
		return treeItem;
	}
	
	/**
	 * 文件选择事件
	 */
	private EventHandler<ActionEvent> selectAction = (event) -> {
		CheckBox checkBox = (CheckBox) event.getSource();
		boolean selected = checkBox.isSelected();
		String prefix = checkBoxMap.entrySet()
		.stream()
		.filter(entry -> entry.getValue() == checkBox)
		.map(entry -> entry.getKey())
		.findFirst().get();
		checkBoxMap.entrySet()
		.stream()
		.filter(entry -> entry.getKey().startsWith(prefix))
		.forEach(entry -> entry.getValue().setSelected(selected));
		buttonSize();
	};
	
	/**
	 * 设置按钮文本
	 */
	private void buttonSize() {
		download.setText("下载（" + FileUtils.formatSize(size()) + "）");
	}
	
}
