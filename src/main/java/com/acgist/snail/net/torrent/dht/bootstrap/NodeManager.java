package com.acgist.snail.net.torrent.dht.bootstrap;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.acgist.snail.net.torrent.dht.DhtClient;
import com.acgist.snail.pojo.session.NodeSession;
import com.acgist.snail.system.config.DhtConfig;
import com.acgist.snail.system.config.SystemConfig;
import com.acgist.snail.utils.ArrayUtils;
import com.acgist.snail.utils.CollectionUtils;
import com.acgist.snail.utils.NumberUtils;
import com.acgist.snail.utils.StringUtils;

/**
 * <p>DHT节点管理器</p>
 * <p>协议链接（Kademlia）：https://baike.baidu.com/item/Kademlia</p>
 * <p>BT=DHT、eMule=KAD</p>
 * 
 * TODO：观察是否需要定时清理Node
 * 
 * @author acgist
 * @since 1.0.0
 */
public final class NodeManager {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(NodeManager.class);
	
	private static final NodeManager INSTANCE = new NodeManager();
	
	/**
	 * <p>Token长度：{@value}</p>
	 */
	private static final int TOKEN_LENGTH = 8;
	/**
	 * <p>Node查找时返回的Node列表长度：{@value}</p>
	 */
	private static final int FIND_NODE_SIZE = 8;
	/**
	 * <p>Node查找时分片大小：{@value}</p>
	 */
	private static final int FIND_NODE_SLICE_SIZE = 3;
	/**
	 * <p>Node查找时分片最小列表长度：{@value}</p>
	 */
	private static final int FIND_NODE_MIN_SLICE_SIZE = FIND_NODE_SIZE * FIND_NODE_SLICE_SIZE;
	
	/**
	 * <p>当前客户端的Token</p>
	 */
	private final byte[] token;
	/**
	 * <p>当前客户端的NodeId</p>
	 */
	private final byte[] nodeId;
	/**
	 * <p>节点列表</p>
	 */
	private final List<NodeSession> nodes;
	
	private NodeManager() {
		this.token = buildToken();
		this.nodeId = buildNodeId();
		this.nodes = new ArrayList<>();
	}
	
	public static final NodeManager getInstance() {
		return INSTANCE;
	}
	
	/**
	 * <p>获取系统NodeId</p>
	 * 
	 * @return 系统NodeId
	 */
	public byte[] nodeId() {
		return this.nodeId;
	}
	
	/**
	 * <p>获取系统Token</p>
	 * 
	 * @return 系统Token
	 */
	public byte[] token() {
		return this.token;
	}
	
	/**
	 * <p>生成系统Token</p>
	 * 
	 * @return 系统Token
	 */
	private byte[] buildToken() {
		LOGGER.debug("生成系统Token");
		final byte[] token = new byte[TOKEN_LENGTH];
		final byte[] tokens = (SystemConfig.LETTER + SystemConfig.LETTER_UPPER + SystemConfig.DIGIT).getBytes();
		final int length = tokens.length;
		final Random random = NumberUtils.random();
		for (int index = 0; index < TOKEN_LENGTH; index++) {
			token[index] = tokens[random.nextInt(length)];
		}
		return token;
	}
	
	/**
	 * <p>生成系统NodeId</p>
	 * 
	 * @return 系统NodeId
	 */
	private byte[] buildNodeId() {
		LOGGER.debug("生成系统NodeId");
		final byte[] nodeIds = new byte[DhtConfig.NODE_ID_LENGTH];
		final Random random = NumberUtils.random();
		for (int index = 0; index < DhtConfig.NODE_ID_LENGTH; index++) {
			nodeIds[index] = (byte) random.nextInt(SystemConfig.UNSIGNED_BYTE_MAX);
		}
		return nodeIds;
	}
	
	/**
	 * <p>获取所有节点的拷贝</p>
	 * 
	 * @return 所有节点的拷贝
	 */
	public List<NodeSession> nodes() {
		synchronized (this.nodes) {
			return new ArrayList<>(this.nodes);
		}
	}

	/**
	 * <p>注册{@linkplain DhtConfig#nodes() 默认节点}</p>
	 */
	public void register() {
		final var nodes = DhtConfig.getInstance().nodes();
		if(CollectionUtils.isNotEmpty(nodes)) {
			nodes.forEach((nodeId, address) -> {
				LOGGER.debug("注册默认节点：{}-{}", nodeId, address);
				final int index = address.lastIndexOf(':');
				if(index != -1) {
					final String host = address.substring(0, index);
					final String port = address.substring(index + 1);
					if(StringUtils.isNotEmpty(host) && StringUtils.isNumeric(port)) {
						newNodeSession(StringUtils.unhex(nodeId), host, Integer.valueOf(port));
					}
				} else {
					LOGGER.warn("节点格式错误：{}-{}", nodeId, address);
				}
			});
			sortNodes(); // 排序
		}
	}
	
	/**
	 * <p>添加DHT节点</p>
	 * <p>先验证状态，通过验证后加入系统节点列表，设置为可用状态。</p>
	 * 
	 * @param host 地址
	 * @param port 端口
	 * 
	 * @return DHT节点
	 */
	public NodeSession newNodeSession(String host, Integer port) {
		final DhtClient client = DhtClient.newInstance(host, port);
		final NodeSession nodeSession = client.ping();
		if(nodeSession != null) {
			nodeSession.setStatus(NodeSession.Status.AVAILABLE); // 标记可用
		}
		return nodeSession;
	}
	
	/**
	 * <p>添加DHT节点</p>
	 * <p>加入时不验证状态，使用时才验证。</p>
	 * <p>添加完成后需要调用{@link #sortNodes()}进行排序</p>
	 * 
	 * @param nodeId 节点ID
	 * @param host 地址
	 * @param port 端口
	 * 
	 * @return DHT节点
	 */
	public NodeSession newNodeSession(byte[] nodeId, String host, Integer port) {
		synchronized (this.nodes) {
			NodeSession nodeSession = select(nodeId);
			if(nodeSession == null) {
				nodeSession = NodeSession.newInstance(nodeId, host, port);
				if(nodeSession.getId().length == DhtConfig.NODE_ID_LENGTH) {
					if(LOGGER.isDebugEnabled()) {
						LOGGER.debug("添加Node：{}-{}-{}", StringUtils.hex(nodeId), nodeSession.getHost(), nodeSession.getPort());
					}
					this.nodes.add(nodeSession);
				}
			}
			return nodeSession;
		}
	}

	/**
	 * <p>排序节点</p>
	 * <p>{@linkplain #newNodeSession(byte[], String, Integer) 添加DHT节点}后进行排序，添加大量节点时可以在添加完成后一次性排序。</p>
	 */
	public void sortNodes() {
		synchronized (this.nodes) {
			Collections.sort(this.nodes);
		}
	}
	
	/**
	 * <p>查找节点列表</p>
	 * 
	 * @param target InfoHashHex或者NodeIdHex
	 * 
	 * @return 节点列表
	 */
	public List<NodeSession> findNode(String target) {
		return this.findNode(StringUtils.unhex(target));
	}
	
	/**
	 * <p>查找节点列表</p>
	 * 
	 * @param target InfoHash或者NodeId
	 * 
	 * @return 节点列表
	 * 
	 * TODO：缓存结果（使用WeakReference）
	 */
	public List<NodeSession> findNode(byte[] target) {
		List<NodeSession> nodes; // 筛选节点的副本
		synchronized (this.nodes) {
			nodes = this.nodes.stream()
				// 排除正在验证中的节点
				.filter(node -> node.getStatus() != NodeSession.Status.VERIFY)
				.collect(Collectors.toList());
		}
		return this.findNode(nodes, target, 0, nodes.size());
	}

	/**
	 * <p>查找节点列表</p>
	 * <p>查找时节点列表组成一个环形结构并{@linkplain #FIND_NODE_SLICE_SIZE 分片}，然后在所有分片中选出最接近目标的片段，然后使用该片段继续查找直到找到最小片段为止。</p>
	 * 
	 * @param nodes 节点列表
	 * @param target 目标
	 * @param begin 开始索引
	 * @param end 结束索引
	 * 
	 * @return 节点列表
	 */
	private List<NodeSession> findNode(final List<NodeSession> nodes, final byte[] target, final int begin, final int end) {
		int selectSize; // 当前选择Node的总数量
		final int nodeSize = nodes.size(); // 当前节点的总数量
		if(end > begin) { // 顺序
			selectSize = end - begin;
		} else { // 接头
			selectSize = end + nodeSize - begin;
		}
		if(selectSize < FIND_NODE_MIN_SLICE_SIZE) { // 最小片段选择节点
			return selectNode(nodes, target, begin, end);
		} else { // 分片
			// 分片中Node的数量
			final int sliceSize = selectSize / FIND_NODE_SLICE_SIZE;
			int sliceA, sliceB, sliceC;
			// 分割节点下标
			if(end > begin) { // 顺序
				sliceA = begin;
				sliceB = sliceA + sliceSize;
				sliceC = sliceB + sliceSize;
			} else { // 接头
				sliceA = begin;
				sliceB = (sliceA + sliceSize) % nodeSize;
				sliceC = (sliceB + sliceSize) % nodeSize;
			}
			// 分割节点
			final var nodeA = nodes.get(sliceA);
			final var nodeB = nodes.get(sliceB);
			final var nodeC = nodes.get(sliceC);
			// 节点不同字节索引
			final int diffIndexA = ArrayUtils.diffIndex(nodeA.getId(), target);
			final int diffIndexB = ArrayUtils.diffIndex(nodeB.getId(), target);
			final int diffIndexC = ArrayUtils.diffIndex(nodeC.getId(), target);
			// 选择最接近的片段
			if(diffIndexA > diffIndexB && diffIndexA > diffIndexC) {
				return findNode(nodes, target, sliceC, sliceB);
			} else if(diffIndexB > diffIndexA && diffIndexB > diffIndexC) {
				return findNode(nodes, target, sliceA, sliceC);
			} else if(diffIndexC > diffIndexA && diffIndexC > diffIndexB) {
				return findNode(nodes, target, sliceB, sliceA);
			} else { // 三个值一致时选择节点
				return this.selectNode(nodes, target, begin, end);
			}
		}
	}

	/**
	 * <p>选择节点列表</p>
	 * <p>排序查找最近的节点</p>
	 * <p>如果节点处于未知状态则修改为验证状态</p>
	 * 
	 * @param nodes 节点列表
	 * @param target 目标
	 * @param begin 开始索引
	 * @param end 结束索引
	 * 
	 * @return 节点列表
	 */
	private List<NodeSession> selectNode(final List<NodeSession> nodes, final byte[] target, final int begin, final int end) {
		Stream<NodeSession> select;
		if(begin < end) {
			select = nodes.stream().skip(begin).limit(end - begin);
		} else {
			select = Stream.concat(nodes.stream().limit(end), nodes.stream().skip(begin));
		}
		return select
			.map(node -> Map.entry(ArrayUtils.xor(node.getId(), target), node))
			.sorted((a, b) -> ArrayUtils.compareUnsigned(a.getKey(), b.getKey()))
			.map(Map.Entry::getValue)
			.limit(FIND_NODE_SIZE)
			.peek(node -> {
				// 设置状态
				if(node.getStatus() == NodeSession.Status.UNUSE) {
					node.setStatus(NodeSession.Status.VERIFY);
				}
			})
			.collect(Collectors.toList());
	}
	
	/**
	 * <p>标记节点为可用状态</p>
	 * 
	 * @param response 响应
	 */
	public void available(DhtResponse response) {
		if(response != null) {
			synchronized (this.nodes) {
				final NodeSession node = select(response.getNodeId());
				if(node != null) {
					node.setStatus(NodeSession.Status.AVAILABLE);
				}
			}
		}
	}
	
	/**
	 * <p>选择节点</p>
	 * 
	 * @param nodeId 节点ID
	 * 
	 * @return 节点
	 */
	private NodeSession select(byte[] nodeId) {
		for (NodeSession nodeSession : this.nodes) {
			if(ArrayUtils.equals(nodeId, nodeSession.getId())) {
				return nodeSession;
			}
		}
		return null;
	}
	
}
