package com.acgist.snail.gui.torrent;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
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
	 * 选择器
	 */
	private Map<String, Selecter> selecter;

	/**
	 * 选择器
	 * 
	 * @param name 任务名称
	 * @param download 下载按钮
	 * @param tree 属性菜单
	 */
	private TorrentFileSelecter(String name, Button download, TreeView<HBox> tree) {
		this.selecter = new HashMap<>();
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
		this.selecter.values().stream()
			.filter(value -> value.isSelected())
			.map(value -> value.getSize())
			.forEach(size -> totalSize.addAndGet(size));
		return totalSize.longValue();
	}
	
	/**
	 * 获取选择文件的列表
	 */
	public List<String> description() {
		return this.selecter.entrySet().stream()
			.filter(entry -> entry.getValue().isSelected()) // 选中
			.filter(entry -> entry.getValue().isFile()) // 文件
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
			this.selecter.entrySet().stream()
				.filter(entry -> list.contains(entry.getKey()))
				.forEach(entry -> entry.getValue().setSelected(true));
		} else { // 自动选择
			final var avgSize = this.selecter.values().stream()
				.collect(Collectors.averagingLong(Selecter::getSize));
			this.selecter.entrySet().stream()
				.filter(entry -> {
					return entry.getValue().isFile() && // 文件
						entry.getValue().getSize() >= avgSize;
				}).forEach(entry -> entry.getValue().setSelected(true));
		}
		selectFolder();
		buttonSize();
	}
	
	/**
	 * @param parent 父节点
	 * @param path 路径
	 * @param name 名称
	 * @param size 大小
	 */
	private TreeItem<HBox> builcTreeItem(TreeItem<HBox> parent, String path, String name, Long size) {
		if(this.selecter.containsKey(path)) {
			return this.selecter.get(path).getTreeItem();
		}
		final CheckBox checkBox = new CheckBox(name);
		checkBox.setPrefWidth(400);
		checkBox.setOnAction(this.selectAction);
		final HBox box = new HBox(checkBox);
		if(size != null) { // 设置文件大小
			final Text text = new Text(FileUtils.formatSize(size));
			box.getChildren().add(text);
		}
		final TreeItem<HBox> treeItem = new TreeItem<HBox>(box);
		this.selecter.put(path, new Selecter(path, size, checkBox, treeItem));
		if(parent != null) {
			parent.getChildren().add(treeItem);
		}
		return treeItem;
	}
	
	/**
	 * 选择父目录
	 */
	private void selectFolder() {
		final Set<TreeItem<HBox>> parents = new HashSet<>();
		// 获取父目录
		this.selecter.values().stream()
			.filter(value -> value.isFile())
			.filter(value -> value.isSelected())
			.forEach(value -> {
				var parent = value.getTreeItem();
				while(parent.getParent() != null) {
					parent = parent.getParent();
					parents.add(parent);
				}
			});
		// 选择父目录
		this.selecter.values().stream()
			.filter(value -> parents.contains(value.getTreeItem()))
			.forEach(value -> value.setSelected(true));
	}
	
	/**
	 * 设置按钮文本
	 */
	private void buttonSize() {
		this.download.setText("下载（" + FileUtils.formatSize(size()) + "）");
	}
	
	/**
	 * 选择框事件
	 */
	private EventHandler<ActionEvent> selectAction = (event) -> {
		final CheckBox checkBox = (CheckBox) event.getSource();
		final boolean selected = checkBox.isSelected();
		final String prefix = this.selecter.entrySet().stream()
			.filter(entry -> entry.getValue().getCheckBox() == checkBox)
			.map(entry -> entry.getKey())
			.findFirst().get();
		// 子目录
		this.selecter.entrySet().stream()
			.filter(entry -> entry.getKey().startsWith(prefix))
			.forEach(entry -> entry.getValue().setSelected(selected));
		selectFolder();
		buttonSize();
	};
	
}

/**
 * 选择文件
 */
class Selecter {

	private final String path; // 路径
	private final long size; // 文件大小：文件夹=0；
	private final boolean file; // 是否是文件：true=文件；false=文件夹；
	private final CheckBox checkBox; // 选择框
	private final TreeItem<HBox> treeItem; // 树节点

	public Selecter(String path, Long size, CheckBox checkBox, TreeItem<HBox> treeItem) {
		this.path = path;
		this.size = size == null ? 0 : size;
		this.file = size == null ? false : true;
		this.checkBox = checkBox;
		this.treeItem = treeItem;
	}

	/**
	 * 判断是否选中
	 */
	public boolean isSelected() {
		return this.checkBox.isSelected();
	}

	/**
	 * 设置是否选中
	 * 
	 * @param selected true-选择；false-未选中；
	 */
	public void setSelected(boolean selected) {
		this.checkBox.setSelected(selected);
	}
	
	public String getPath() {
		return path;
	}

	public long getSize() {
		return size;
	}

	public boolean isFile() {
		return file;
	}

	public CheckBox getCheckBox() {
		return checkBox;
	}

	public TreeItem<HBox> getTreeItem() {
		return treeItem;
	}

}
