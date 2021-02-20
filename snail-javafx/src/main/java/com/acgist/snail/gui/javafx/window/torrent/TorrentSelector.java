package com.acgist.snail.gui.javafx.window.torrent;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

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
 * <p>BT任务下载文件选择器</p>
 * 
 * @author acgist
 */
public final class TorrentSelector {

	/**
	 * <p>下载按钮</p>
	 */
	private final Button download;
	/**
	 * <p>树形菜单根节点</p>
	 */
	private final TreeItem<HBox> root;
	/**
	 * <p>文件选择器Map</p>
	 * <p>文件路径=文件选择器</p>
	 */
	private final Map<String, Selector> selectors = new HashMap<>();

	/**
	 * @param name 任务名称
	 * @param download 下载按钮
	 * @param tree 树形菜单
	 */
	private TorrentSelector(String name, Button download, TreeView<HBox> tree) {
		this.root = this.buildTreeItem(null, "", name, null);
		this.root.setExpanded(true);
		tree.setRoot(this.root);
		this.download = download;
	}
	
	/**
	 * <p>创建BT任务下载文件选择器</p>
	 * 
	 * @param name 任务名称
	 * @param download 下载按钮
	 * @param tree 树形菜单
	 * 
	 * @return {@link TorrentSelector}
	 */
	public static final TorrentSelector newInstance(String name, Button download, TreeView<HBox> tree) {
		return new TorrentSelector(name, download, tree);
	}

	/**
	 * <p>创建文件菜单</p>
	 * 
	 * @param torrentFile 文件
	 */
	public void build(TorrentFile torrentFile) {
		final String path = torrentFile.path();
		final Long size = torrentFile.getLength();
		String fileName = path;
		TreeItem<HBox> parent = this.root;
		if(path.contains(TorrentFile.SEPARATOR)) {
			// 包含路径
			final StringBuilder parentPath = new StringBuilder("");
			final String[] paths = path.split(TorrentFile.SEPARATOR);
			// 创建路径菜单：数量大时forEach效率很低
			for (int index = 0; index < paths.length - 1; index++) {
				final String value = paths[index];
				parentPath.append(value).append(TorrentFile.SEPARATOR);
				parent = this.buildTreeItem(parent, parentPath.toString(), value, null);
			}
			fileName = paths[paths.length - 1];
		}
		// 创建文件菜单
		this.buildTreeItem(parent, path, fileName, size);
	}
	
	/**
	 * <p>创建文件菜单</p>
	 * 
	 * @param parent 上级节点
	 * @param path 文件路径
	 * @param name 文件名称
	 * @param size 文件大小
	 * 
	 * @return 文件菜单
	 */
	private TreeItem<HBox> buildTreeItem(TreeItem<HBox> parent, String path, String name, Long size) {
		final Selector oldSelector = this.selectors.get(path);
		if(oldSelector != null) {
			// 如果已经创建跳过（路径菜单可能重复）
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
		this.selectors.put(path, new Selector(path, size, nameCheckBox, treeItem));
		if(parent != null) {
			// 根节点没有上级节点
			parent.getChildren().add(treeItem);
		}
		return treeItem;
	}
	
	/**
	 * <p>获取选择文件流</p>
	 * 
	 * @return 选择文件流
	 */
	private Stream<Selector> selectedSelectors() {
		return this.selectors.values().stream()
			.filter(Selector::isSelected)
			.filter(Selector::isFile);
	}
	
	/**
	 * <p>获取选择文件大小</p>
	 * 
	 * @return 选择文件大小
	 */
	public Long size() {
		return this.selectedSelectors()
			.collect(Collectors.summingLong(Selector::getSize));
	}
	
	/**
	 * <p>获取选择文件列表</p>
	 * 
	 * @return 选择文件列表
	 */
	public List<String> description() {
		return this.selectedSelectors()
			.map(Selector::getPath)
			.collect(Collectors.toList());
	}

	/**
	 * <p>设置选择文件</p>
	 * <p>如果没有选择文件使用自动选择（选择大于平均值的文件）</p>
	 * 
	 * @param taskSession 任务信息
	 */
	public void select(ITaskSession taskSession) {
		final var selectedFileList = taskSession.multifileSelected();
		if(CollectionUtils.isNotEmpty(selectedFileList)) {
			// 已选择文件
			this.selectors.values().stream()
				.filter(selector -> selectedFileList.contains(selector.getPath()))
				.forEach(Selector::setSelected);
		} else {
			// 未选择文件
			// 计算平均值
			final var avgSize = this.selectors.values().stream()
				.collect(Collectors.averagingLong(Selector::getSize));
			this.selectors.values().stream()
				.filter(Selector::isFile)
				// 大于平均值
				.filter(selector -> selector.getSize() >= avgSize)
				.forEach(Selector::setSelected);
		}
		this.selectParentFolder();
		this.buildButtonSize();
	}
	
	/**
	 * <p>设置按钮文本</p>
	 */
	private void buildButtonSize() {
		this.download.setText("下载（" + FileUtils.formatSize(this.size()) + "）");
	}
	
	/**
	 * <p>选择上级目录</p>
	 * <p>选中文件时同时选中所有上级目录</p>
	 */
	private void selectParentFolder() {
		final List<TreeItem<HBox>> parents = new ArrayList<>();
		// 所有上级目录
		this.selectedSelectors().forEach(selector -> {
			var parent = selector.getTreeItem();
			while(parent.getParent() != null) {
				parent = parent.getParent();
				parents.add(parent);
			}
		});
		// 选择上级目录
		this.selectors.values().stream()
			.filter(selector -> parents.contains(selector.getTreeItem()))
			.forEach(Selector::setSelected);
	}
	
	/**
	 * <p>选择框事件</p>
	 * <p>选择下级目录、选择上级目录、设置按钮文本</p>
	 */
	private EventHandler<ActionEvent> selectAction = event -> {
		final CheckBox checkBox = (CheckBox) event.getSource();
		final boolean selected = checkBox.isSelected();
		// 获取选择文件前缀
		final String prefix = this.selectors.values().stream()
			.filter(selector -> selector.getCheckBox() == checkBox)
			.map(Selector::getPath)
			.findFirst()
			.orElse("");
		// 选择下级目录
		this.selectors.values().stream()
			.filter(selector -> selector.getPath().startsWith(prefix))
			.forEach(selector -> selector.setSelected(selected));
		this.selectParentFolder();
		this.buildButtonSize();
	};
	
	/**
	 * <p>文件选择器</p>
	 * 
	 * @author acgist
	 */
	protected static final class Selector {
		
		/**
		 * <p>文件路径</p>
		 */
		private final String path;
		/**
		 * <p>文件大小</p>
		 * <p>目录为零</p>
		 */
		private final long size;
		/**
		 * <p>是否是文件</p>
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
		
		/**
		 * @param path 文件路径
		 * @param size 文件大小
		 * @param checkBox 选择框
		 * @param treeItem 文件菜单节点
		 */
		protected Selector(String path, Long size, CheckBox checkBox, TreeItem<HBox> treeItem) {
			this.path = path;
			this.size = size == null ? 0L : size;
			this.file = this.size != 0L;
			this.checkBox = checkBox;
			this.treeItem = treeItem;
		}
		
		/**
		 * <p>判断是否选中</p>
		 * 
		 * @return 是否选中
		 */
		public boolean isSelected() {
			return this.checkBox.isSelected();
		}

		/**
		 * <p>设置选中</p>
		 */
		public void setSelected() {
			this.setSelected(true);
		}
		
		/**
		 * <p>设置是否选中</p>
		 * 
		 * @param selected 是否选中
		 */
		public void setSelected(boolean selected) {
			this.checkBox.setSelected(selected);
		}
		
		/**
		 * <p>获取文件路径</p>
		 * 
		 * @return 文件路径
		 */
		public String getPath() {
			return this.path;
		}
		
		/**
		 * <p>获取文件大小</p>
		 * 
		 * @return 文件大小
		 */
		public long getSize() {
			return this.size;
		}
		
		/**
		 * <p>判断是否是文件</p>
		 * 
		 * @return 是否是文件
		 */
		public boolean isFile() {
			return this.file;
		}
		
		/**
		 * <p>获取选择框</p>
		 * 
		 * @return 选择框
		 */
		public CheckBox getCheckBox() {
			return this.checkBox;
		}
		
		/**
		 * <p>获取文件菜单节点</p>
		 * 
		 * @return 文件菜单节点
		 */
		public TreeItem<HBox> getTreeItem() {
			return this.treeItem;
		}
		
	}
	
}
