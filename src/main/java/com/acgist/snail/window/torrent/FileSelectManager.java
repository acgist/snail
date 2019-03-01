package com.acgist.snail.window.torrent;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

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
	private Map<CheckBox, Long> sizeMap = new HashMap<>();
	private Map<String, CheckBox> checkBoxMap = new HashMap<>();
	private Map<String, TreeItem<HBox>> treeItemMap = new HashMap<>();

	/**
	 * 选择器
	 * @param name 任务名称
	 * @param download 下载按钮
	 * @param tree 属性菜单
	 */
	public FileSelectManager(String name, Button download, TreeView<HBox> tree) {
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
	 * 选择的文件
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
			Text text = new Text(FileUtils.size(size));
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
		.filter(entry -> {
			return entry.getKey().startsWith(prefix);
		}).forEach(entry -> {
			entry.getValue().setSelected(selected);
		});
		long totalSize = selectSize();
		download.setText("下载（" + FileUtils.size(totalSize) + "）");
	};
	
	/**
	 * 已选择文件总大小
	 */
	private long selectSize() {
		AtomicLong totalSize = new AtomicLong(0L);
		checkBoxMap.entrySet()
		.stream()
		.filter(entry -> entry.getValue().isSelected())
		.forEach(entry -> {
			Long size = sizeMap.get(entry.getValue());
			if(size != null) {
				totalSize.addAndGet(size);
			}
		});
		return totalSize.longValue();
	}
	
}
