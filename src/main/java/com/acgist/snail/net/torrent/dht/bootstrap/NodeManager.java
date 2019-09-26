package com.acgist.snail.net.torrent.dht.bootstrap;

import java.net.InetSocketAddress;
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
 * <p>Node管理器</p>
 * <p>DHT节点管理器。</p>
 * TODO：观察是否需要清理Node
 * 
 * <p>
 * Kademlia：https://baike.baidu.com/item/Kademlia<br>
 * BT=DHT、eMule=KAD
 * </p>
 * 
 * @author acgist
 * @since 1.0.0
 */
public class NodeManager {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(NodeManager.class);
	
	/**
	 * Node最大数量，超过这个数量会均匀剔除多余Node
	 */
	public static final int MAX_NODE_SIZE = 1024;
	/**
	 * NodeId长度
	 */
	public static final int NODE_ID_LENGTH = 20;
	/**
	 * Token长度
	 */
	public static final int TOKEN_LENGTH = 8;
	/**
	 * Node查找时返回的Node列表长度
	 */
	public static final int NODE_FIND_SIZE = 8;
	/**
	 * Node查找时分片大小
	 */
	public static final int NODE_FIND_SLICE = 3;
	/**
	 * Node查找时分片最小列表长度
	 */
	public static final int NODE_FIND_SLICE_SIZE = NODE_FIND_SIZE * NODE_FIND_SLICE;
	
	/**
	 * 当前客户端的Token
	 */
	private final byte[] token;
	/**
	 * 当前客户端的NodeId
	 */
	private final byte[] nodeId;
	/**
	 * 节点列表
	 */
	private final List<NodeSession> nodes;
	
	private NodeManager() {
		this.token = buildToken();
		this.nodeId = buildNodeId();
		this.nodes = new ArrayList<>();
	}
	
	private static final NodeManager INSTANCE = new NodeManager();
	
	public static final NodeManager getInstance() {
		return INSTANCE;
	}
	
	/**
	 * 当前系统nodeId
	 */
	public byte[] nodeId() {
		return nodeId;
	}
	
	/**
	 * 当前系统token
	 */
	public byte[] token() {
		return token;
	}
	
	/**
	 * 生成NodeId
	 */
	private byte[] buildNodeId() {
		final byte[] nodeIds = new byte[NODE_ID_LENGTH];
		final Random random = NumberUtils.random();
		for (int index = 0; index < NODE_ID_LENGTH; index++) {
			nodeIds[index] = (byte) random.nextInt(SystemConfig.UNSIGNED_BYTE_SIZE);
		}
		return nodeIds;
	}
	
	/**
	 * 生成Token
	 */
	private byte[] buildToken() {
		final byte[] token = new byte[TOKEN_LENGTH];
		final byte[] bytes = (SystemConfig.LETTER + SystemConfig.LETTER_UPPER + SystemConfig.DIGIT).getBytes();
		final int length = bytes.length;
		final Random random = NumberUtils.random();
		for (int index = 0; index < TOKEN_LENGTH; index++) {
			token[index] = bytes[random.nextInt(length)];
		}
		return token;
	}
	
	/**
	 * 获取所有的Nodes的拷贝
	 */
	public List<NodeSession> nodes() {
		synchronized (this.nodes) {
			return new ArrayList<>(this.nodes);
		}
	}

	/**
	 * 注册默认节点（配置文件）
	 */
	public void register() {
		final var nodes = DhtConfig.getInstance().nodes();
		if(CollectionUtils.isNotEmpty(nodes)) {
			nodes.forEach((nodeId, address) -> {
				LOGGER.debug("注册默认节点：{}-{}", nodeId, address);
				final int index = address.lastIndexOf(":");
				if(index != -1) {
					final String host = address.substring(0, index);
					final String port = address.substring(index + 1);
					if(StringUtils.isNotEmpty(host) && StringUtils.isNumeric(port)) {
						newNodeSession(StringUtils.unhex(nodeId), host, Integer.valueOf(port));
					}
				}
			});
			sortNodes();
		}
	}
	
	/**
	 * <p>添加DHT节点</p>
	 * <p>先验证节点（ping），验证通过加入列表，设置为有效节点。</p>
	 */
	public void newNodeSession(String host, Integer port) {
		final NodeSession nodeSession = verify(host, port);
		if(nodeSession != null) {
			nodeSession.setStatus(NodeSession.STATUS_AVAILABLE); // 标记有效
		}
	}
	
	/**
	 * <p>添加DHT节点</p>
	 * <p>添加Node，新建完成后需要调用{@link #sortNodes()}进行排序。</p>
	 * <p>加入时不验证，使用时进行验证。</p>
	 */
	public NodeSession newNodeSession(byte[] nodeId, String host, Integer port) {
		synchronized (this.nodes) {
			NodeSession nodeSession = select(nodeId);
			if(nodeSession != null) {
				return nodeSession;
			}
			if(nodeSession == null) {
				nodeSession = NodeSession.newInstance(nodeId, host, port);
				if(nodeSession.getId().length == NODE_ID_LENGTH) {
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
	 * <p>排序</p>
	 * <p>在调用{@link #newNodeSession(byte[], String, Integer)}后需要进行排序。</p>
	 */
	public void sortNodes() {
		synchronized (this.nodes) {
			Collections.sort(this.nodes); // 排序
		}
	}
	
	/**
	 * Node验证
	 */
	private NodeSession verify(String host, Integer port) {
		final DhtClient client = DhtClient.newInstance(host, port);
		try {
			return client.ping();
		} catch (Exception e) {
			LOGGER.error("DHT Ping异常", e);
		}
		return null;
	}

	/**
	 * <p>查找Node</p>
	 */
	public List<NodeSession> findNode(String target) {
		return this.findNode(StringUtils.unhex(target));
	}
	
	/**
	 * <p>查找Node</p>
	 * <p>筛选{@linkplain NodeSession#STATUS_UNUSE 未使用}和{@linkplain NodeSession#STATUS_AVAILABLE 可用}的节点。</p>
	 */
	public List<NodeSession> findNode(byte[] target) {
		List<NodeSession> nodes; // 筛选节点的副本
		synchronized (this.nodes) {
			nodes = this.nodes.stream()
				.filter(node -> node.getStatus() != NodeSession.STATUS_VERIFY)
				.collect(Collectors.toList());
		}
		return this.findNode(nodes, target, 0, nodes.size());
	}

	/**
	 * <p>查找Node</p>
	 * <p>查找最近（异或运算）的一段NodeId。</p>
	 * <p>查找时Node是一个环形结构，按照{@linkplain #NODE_FIND_SLICE 大小}分片。</p>
	 * 
	 * @param target 目标NodeId
	 * @param begin 开始序号
	 * @param end 结束序号
	 * 
	 * @return 节点，{@linkplain #NODE_FIND_SIZE 数量}
	 */
	private List<NodeSession> findNode(final List<NodeSession> nodes, final byte[] target, final int begin, final int end) {
		int selectSize; // 当前选择Node的总数量
		final int nodeSize = nodes.size(); // 当前节点的总数量
		if(end > begin) { // 顺序
			selectSize = end - begin;
		} else { // 接头
			selectSize = end + nodeSize - begin;
		}
		if(selectSize < NODE_FIND_SLICE_SIZE) { // 小于最小列表时开始排序返回最近列表。
			return selectNode(nodes, target, begin, end);
		} else { // 分片
			// 下标，如果下标大于节点数量，表示从头开始选择。
			final int sliceSize = selectSize / NODE_FIND_SLICE; // 分片中Node的数量
			int sliceA, sliceB, sliceC;
			if(end > begin) {
				sliceA = begin;
				sliceB = sliceA + sliceSize;
				sliceC = sliceB + sliceSize;
			} else {
				sliceA = begin;
				sliceB = (sliceA + sliceSize) % nodeSize;
				sliceC = (sliceB + sliceSize) % nodeSize;
			}
			// 节点
			final var nodeA = nodes.get(sliceA);
			final var nodeB = nodes.get(sliceB);
			final var nodeC = nodes.get(sliceC);
			// 节点不同字节序号
			final int diffIndexA = diffIndex(nodeA.getId(), target);
			final int diffIndexB = diffIndex(nodeB.getId(), target);
			final int diffIndexC = diffIndex(nodeC.getId(), target);
			if(diffIndexA > diffIndexB && diffIndexA > diffIndexC) {
				return findNode(nodes, target, sliceC, sliceB);
			} else if(diffIndexB > diffIndexA && diffIndexB > diffIndexC) {
				return findNode(nodes, target, sliceA, sliceC);
			} else if(diffIndexC > diffIndexA && diffIndexC > diffIndexB) {
				return findNode(nodes, target, sliceB, sliceA);
			} else { // 如果三个一致时直接选择节点。
				return this.selectNode(nodes, target, begin, end);
			}
		}
	}

	/**
	 * <p>选择节点</p>
	 * <p>排序查找最近的节点。</p>
	 * <p>选中的节点如果没有使用过标记为使用中。</p>
	 */
	private List<NodeSession> selectNode(final List<NodeSession> nodes, final byte[] target, final int begin, final int end) {
		Stream<NodeSession> select;
		if(begin < end) {
			select = nodes.stream().skip(begin).limit(end - begin);
		} else {
			select = Stream.concat(nodes.stream().limit(end), nodes.stream().skip(begin));
		}
		return select
			.map(node -> {
				return Map.entry(xor(node.getId(), target), node);
			})
			.sorted((a, b) -> {
				return ArrayUtils.compareUnsigned(a.getKey(), b.getKey());
			})
			.map(Map.Entry::getValue)
			.limit(NODE_FIND_SIZE)
			.peek(node -> {
				if(node.getStatus() == NodeSession.STATUS_UNUSE) {
					node.setStatus(NodeSession.STATUS_VERIFY);
				}
			})
			.collect(Collectors.toList());
	}
	
	/**
	 * 设置token，如果对应nodeId不存在，这加入网络。
	 */
	public void token(byte[] nodeId, Request request, byte[] token) {
		final InetSocketAddress socketAddress = request.getSocketAddress();
		synchronized (this.nodes) {
			NodeSession old = select(nodeId);
			if(old == null) {
				old = newNodeSession(nodeId, socketAddress.getHostString(), socketAddress.getPort());
				this.sortNodes();
			}
			old.setToken(token);
		}
	}
	
	/**
	 * 标记可用节点：有相应的节点标记为可用节点。
	 */
	public void available(Response response) {
		if(response != null) {
			synchronized (this.nodes) {
				final NodeSession node = select(response.getNodeId());
				if(node != null) {
					node.setStatus(NodeSession.STATUS_AVAILABLE);
				}
			}
		}
	}
	
	/**
	 * 选择Node。
	 */
	private NodeSession select(byte[] nodeId) {
		for (NodeSession nodeSession : this.nodes) {
			if(ArrayUtils.equals(nodeId, nodeSession.getId())) {
				return nodeSession;
			}
		}
		return null;
	}
	
	/**
	 * 异或运算，返回十六进制字符串。
	 */
	public static final byte[] xor(byte[] source, byte[] target) {
		final byte[] value = new byte[NODE_ID_LENGTH];
		for (int index = 0; index < NODE_ID_LENGTH; index++) {
			value[index] = (byte) (source[index] ^ target[index]);
		}
		return value;
	}
	
	/**
	 * <p>获取节点出现不同字节的位数。</p>
	 * <p>不同的位数越小，表示差距越大，反之差距越小。</p>
	 */
	public static final int diffIndex(byte[] source, byte[] target) {
		for (int index = 0; index < NODE_ID_LENGTH; index++) {
			if(source[index] != target[index]) {
				return index;
			}
		}
		return NODE_ID_LENGTH;
	}

}
