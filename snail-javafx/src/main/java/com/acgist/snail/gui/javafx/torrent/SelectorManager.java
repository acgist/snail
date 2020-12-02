package com.acgist.snail.gui.javafx.torrent;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;

import com.acgist.snail.gui.javafx.Tooltips;
import com.acgist.snail.pojo.ITaskSession;
import com.acgist.snail.pojo.bean.TorrentFile;
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
 * <p>BT文件选择器</p>
 * 
 * @author acgist
 * @since 1.0.0
 */
public final class SelectorManager {

	/**
	 * <p>下载按钮</p>
	 */
	private final Button download;
	/**
	 * <p>树形菜单根节点</p>
	 */
	private final TreeItem<HBox> root;
	/**
	 * <p>选择器Map</p>
	 * <p>文件路径=选择文件</p>
	 */
	private final Map<String, Selector> selector = new HashMap<>();

	private SelectorManager(String name, Button download, TreeView<HBox> tree) {
		final TreeItem<HBox> root = this.buildTreeItem(null, "", name, null);
		root.setExpanded(true);
		tree.setRoot(root);
		this.root = root;
		this.download = download;
	}
	
	/**
	 * <p>创建BT文件选择器</p>
	 * 
	 * @param name 任务名称
	 * @param download 下载按钮
	 * @param tree 树形菜单
	 * 
	 * @return {@link SelectorManager}
	 */
	public static final SelectorManager newInstance(String name, Button download, TreeView<HBox> tree) {
		return new SelectorManager(name, download, tree);
	}

	/**
	 * <p>创建文件菜单</p>
	 * 
	 * @param path 文件路径
	 * @param size 文件大小
	 */
	public void build(String path, Long size) {
		String name = path;
		TreeItem<HBox> parent = this.root;
		// 包含路径时创建路径菜单
		if(path.contains(TorrentFile.SEPARATOR)) {
			TreeItem<HBox> treeItem = null;
			final StringBuilder parentPath = new StringBuilder("");
			final String[] paths = path.split(TorrentFile.SEPARATOR);
			// 创建路径菜单
			for (int index = 0; index < paths.length - 1; index++) { // 数量大时forEach效率很低
				final String value = paths[index];
				parentPath.append(value).append(TorrentFile.SEPARATOR);
				treeItem = buildTreeItem(parent, parentPath.toString(), value, null);
				parent = treeItem;
			}
			name = paths[paths.length - 1];
		}
		// 创建文件菜单
		buildTreeItem(parent, path, name, size);
	}
	
	/**
	 * <p>创建文件菜单</p>
	 * 
	 * @param parent 上级节点
	 * @param path 文件路径
	 * @param name 文件名称
	 * @param size 文件大小
	 */
	private TreeItem<HBox> buildTreeItem(TreeItem<HBox> parent, String path, String name, Long size) {
		final Selector oldSelector = this.selector.get(path);
		if(oldSelector != null) { // 如果已经创建跳过：路径菜单
			return oldSelector.getTreeItem();
		}
		final CheckBox checkBox = new CheckBox(name);
		checkBox.setPrefWidth(500);
		checkBox.setTooltip(Tooltips.newTooltip(name));
		checkBox.setOnAction(this.selectAction);
		final HBox box = new HBox(checkBox);
		// 设置文件大小
		if(size != null) {
			final Text text = new Text(FileUtils.formatSize(size));
			box.getChildren().add(text);
		}
		final TreeItem<HBox> treeItem = new TreeItem<>(box);
		this.selector.put(path, new Selector(path, size, checkBox, treeItem));
		if(parent != null) { // 根节点没有上级节点
			parent.getChildren().add(treeItem);
		}
		return treeItem;
	}
	
	/**
	 * <p>获取选择文件大小</p>
	 * 
	 * @return 选择文件大小
	 */
	public Long size() {
		return this.selector.values().stream()
			.filter(value -> value.isSelected()) // 选中
			.filter(value -> value.isFile()) // 文件
			.collect(Collectors.summingLong(value -> value.getSize()));
	}
	
	/**
	 * <p>获取选择文件列表</p>
	 * 
	 * @return 选择文件列表
	 */
	public List<String> description() {
		return this.selector.entrySet().stream()
			.filter(entry -> entry.getValue().isFile()) // 文件
			.filter(entry -> entry.getValue().isSelected()) // 选中
			.map(Entry::getKey)
			.collect(Collectors.toList());
	}

	/**
	 * <p>设置选择文件</p>
	 * <p>如果没有选中文件使用自动选择：选择大于平均值的文件</p>
	 * 
	 * @param taskSession 任务信息
	 */
	public void select(ITaskSession taskSession) {
		final var list = taskSession.multifileSelected();
		if(CollectionUtils.isNotEmpty(list)) { // 已选择文件
			this.selector.entrySet().stream()
				.filter(entry -> list.contains(entry.getKey()))
				.forEach(entry -> entry.getValue().setSelected(true));
		} else { // 未选择文件：自动选择
			// 计算平均值
			final var avgSize = this.selector.values().stream()
				.collect(Collectors.averagingLong(Selector::getSize));
			this.selector.entrySet().stream()
				.filter(entry -> {
					return
						entry.getValue().isFile() && // 文件
						entry.getValue().getSize() >= avgSize; // 大于平均值
				})
				.forEach(entry -> entry.getValue().setSelected(true));
		}
		selectParentFolder();
		buttonSize();
	}
	
	/**
	 * <p>选择上级目录</p>
	 * <p>选中文件时同时选中所有上级目录</p>
	 */
	private void selectParentFolder() {
		final List<TreeItem<HBox>> parents = new ArrayList<>();
		// 所有上级目录
		this.selector.values().stream()
			.filter(value -> value.isFile())
			.filter(value -> value.isSelected())
			.forEach(value -> {
				var parent = value.getTreeItem();
				while(parent.getParent() != null) {
					parent = parent.getParent();
					parents.add(parent);
				}
			});
		// 选择上级目录
		this.selector.values().stream()
			.filter(value -> parents.contains(value.getTreeItem()))
			.forEach(value -> value.setSelected(true));
	}
	
	/**
	 * <p>设置按钮文本</p>
	 */
	private void buttonSize() {
		this.download.setText("下载（" + FileUtils.formatSize(size()) + "）");
	}
	
	/**
	 * <p>选择框事件</p>
	 * <p>选择下级目录、选择上级目录、设置按钮文本</p>
	 */
	private EventHandler<ActionEvent> selectAction = event -> {
		final CheckBox checkBox = (CheckBox) event.getSource();
		final boolean selected = checkBox.isSelected();
		// 前缀
		final String prefix = this.selector.entrySet().stream()
			.filter(entry -> entry.getValue().getCheckBox() == checkBox)
			.map(entry -> entry.getKey())
//			.filter(Objects::nonNull) // 绝对有值
			.findFirst()
			.orElse("");
		// 选择下级目录
		this.selector.entrySet().stream()
			.filter(entry -> entry.getKey().startsWith(prefix))
			.forEach(entry -> entry.getValue().setSelected(selected));
		selectParentFolder();
		buttonSize();
	};
	
	/**
	 * <p>选择文件</p>
	 */
	protected static final class Selector {
		
		/**
		 * <p>文件路径</p>
		 */
		private final String path;
		/**
		 * <p>文件大小</p>
		 * <p>目录等于{@code 0}</p>
		 */
		private final long size;
		/**
		 * <p>是否是文件</p>
		 * <p>{@code true}-文件；{@code false}-目录；</p>
		 */
		private final boolean file;
		/**
		 * <p>选择框</p>
		 */
		private final CheckBox checkBox;
		/**
		 * <p>文件菜单节点</p>
		 */
		private final TreeItem<HBox> treeItem;
		
		public Selector(String path, Long size, CheckBox checkBox, TreeItem<HBox> treeItem) {
			this.path = path;
			this.size = (size == null || size == 0L) ? 0 : size;
			this.file = (size == null || size == 0L) ? false : true;
			this.checkBox = checkBox;
			this.treeItem = treeItem;
		}
		
		/**
		 * <p>判断是否被选中</p>
		 * 
		 * @return {@code true}-选中；{@code false}-没有选中；
		 */
		public boolean isSelected() {
			return this.checkBox.isSelected();
		}
		
		/**
		 * <p>设置是否被选中</p>
		 * 
		 * @param selected {@code true}-选中；{@code false}-没有选中；
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
	
}
