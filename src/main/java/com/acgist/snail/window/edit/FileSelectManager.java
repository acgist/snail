package com.acgist.snail.window.edit;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;

import com.acgist.snail.utils.FileUtils;
import com.acgist.snail.utils.StringUtils;

import javafx.scene.control.CheckBox;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import javafx.scene.layout.HBox;
import javafx.scene.text.Text;

/**
 * 路径选择器
 */
public class FileSelectManager {

	private TreeItem<HBox> root;
	private Map<String, CheckBox> checkBoxMap = new HashMap<>();
	private Map<String, TreeItem<HBox>> treeItemMap = new HashMap<>();

	public FileSelectManager(String name, TreeView<HBox> tree) {
		TreeItem<HBox> root = builcTreeItem(null, "", name, null);
		root.setExpanded(true);
		tree.setRoot(root);
		this.root = root;
	}

	public void build(String path, Long size) {
		String name = path;
		TreeItem<HBox> parent = root;
		if(path.contains("/")) {
			String[] paths = path.split("/");
			String parentPath = "";
			TreeItem<HBox> treeItem = null;
			for (int index = 0; index < paths.length - 1; index++) {
				String value = paths[index];
				parentPath += value + "/";
				treeItem = builcTreeItem(parent, parentPath, value, null);
				parent = treeItem;
			}
			name = paths[paths.length - 1];
		}
		builcTreeItem(parent, path, name, size);
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
			box.getChildren().add(text);
		}
		check(checkBox);
		TreeItem<HBox> treeItem = new TreeItem<HBox>(box);
		checkBoxMap.put(path, checkBox);
		treeItemMap.put(path, treeItem);
		if(parent != null) {
			parent.getChildren().add(treeItem);
		}
		return treeItem;
	}

	private void check(CheckBox checkBox) {
		checkBox.setOnAction((event) -> {
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
		});
	}
	
	/**
	 * 获取选择的文件
	 */
	public List<String> selected() {
		return checkBoxMap.entrySet()
		.stream()
		.filter(entry -> entry.getValue().isSelected())
		.map(Entry::getKey)
		.filter(path -> StringUtils.isNotEmpty(path))
		.filter(path -> !path.endsWith("/"))
		.collect(Collectors.toList());
	}
	
}
