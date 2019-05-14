package com.acgist.snail.system.manager;

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

import com.acgist.snail.net.dht.DhtClient;
import com.acgist.snail.net.dht.bootstrap.Request;
import com.acgist.snail.net.dht.bootstrap.Response;
import com.acgist.snail.pojo.session.NodeSession;
import com.acgist.snail.system.config.SystemConfig;
import com.acgist.snail.utils.ArrayUtils;
import com.acgist.snail.utils.StringUtils;

/**
 * Node信息
 * TODO：连接失败时剔除、定时剔除多余数据
 */
public class NodeManager {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(NodeManager.class);
	
	/**
	 * Node最大数量，超过这个数量会均匀剔除多余Node
	 */
	public static final int NODE_MAX_SIZE = 1024;
	public static final int NODE_ID_LENGTH = 20;
	public static final int TOKEN_LENGTH = 8;
	public static final int GET_PEER_LENGTH = 100;
	public static final int NODE_FIND_SIZE = 8;
	public static final int NODE_FIND_SLICE = 3; // 分片大小
	public static final int NODE_FIND_SLICE_SIZE = NODE_FIND_SIZE * NODE_FIND_SLICE;
	
	private final byte[] token;
	private final byte[] nodeId;
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
	
	private byte[] buildNodeId() {
		final byte[] bytes = new byte[20];
		final Random random = new Random();
		for (int i = 0; i < 20; i++) {
			bytes[i] = (byte) random.nextInt(SystemConfig.UNSIGNED_BYTE_SIZE);
		}
		return bytes;
	}
	
	private byte[] buildToken() {
		final byte[] token = new byte[TOKEN_LENGTH];
		final byte[] bytes = (SystemConfig.LETTER + SystemConfig.LETTER_UPPER + SystemConfig.DIGIT).getBytes();
		final int length = bytes.length;
		final Random random = new Random();
		for (int index = 0; index < TOKEN_LENGTH; index++) {
			token[index] = bytes[random.nextInt(length)];
		}
		return token;
	}
	
	/**
	 * 所有的Nodes
	 */
	public List<NodeSession> nodes() {
		synchronized (this.nodes) {
			return new ArrayList<>(this.nodes);
		}
	}

	/**
	 * 添加DHT节点，使用Ping然后添加到列表
	 */
	public void newNodeSession(String host, Integer port) {
		final NodeSession nodeSession = verify(host, port);
		if(nodeSession != null) {
			nodeSession.setStatus(NodeSession.STATUS_AVAILABLE); // 标记有效
		}
	}
	
	/**
	 * 新建Node，新建完成后需要调用{@link #sortNodes()}进行排序
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
						LOGGER.debug("添加Node：{}-{}", nodeSession.getHost(), nodeSession.getPort());
					}
					this.nodes.add(nodeSession);
				}
			}
			return nodeSession;
		}
	}

	/**
	 * 排序：在调用{@link #newNodeSession(byte[], String, Integer)}后需要进行排序
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
	 * 查找Node，查找到最近的一段区域，然后异或运算获取最近
	 * 不选择使用中的Node
	 * @param target NodeId
	 * @return Node
	 */
	public List<NodeSession> findNode(String target) {
		return this.findNode(StringUtils.unhex(target));
	}
	
	/**
	 * 查找Node
	 * 不选择使用中的Node
	 */
	public List<NodeSession> findNode(byte[] target) {
		List<NodeSession> nodes;
		synchronized (this.nodes) {
			nodes = this.nodes.stream()
				.filter(node -> node.getStatus() != NodeSession.STATUS_VERIFY)
				.collect(Collectors.toList());
		}
		return this.findNode(nodes, target, 0, nodes.size());
	}

	/**
	 * 查找节点
	 * @param id id
	 * @param begin 开始序号
	 * @param end 结束序号
	 * @return 节点
	 */
	private List<NodeSession> findNode(final List<NodeSession> nodes, final byte[] id, final int begin, final int end) {
		int size;
		if(end > begin) {
			size = end - begin;
		} else {
			size = end + nodes.size() - begin;
		}
		if(size < NODE_FIND_SLICE_SIZE) { // 获取
			return selectNode(nodes, id, begin, end);
		} else { // 分片
			// 下标
			final int sliceSize = (end - begin) / NODE_FIND_SLICE;
			final int sliceA = begin;
			final int sliceB = sliceA + sliceSize;
			final int sliceC = sliceB + sliceSize;
			// 节点
			final var nodeA = nodes.get(sliceA);
			final var nodeB = nodes.get(sliceB);
			final var nodeC = nodes.get(sliceC);
			// 节点相差
			final int indexA = index(nodeA.getId(), id);
			final int indexB = index(nodeB.getId(), id);
			final int indexC = index(nodeC.getId(), id);
			if(indexA > indexB && indexA > indexC) {
				return findNode(nodes, id, sliceC, sliceB);
			} else if(indexB > indexA && indexB > indexC) {
				return findNode(nodes, id, sliceA, sliceC);
			} else if(indexC > indexA && indexC > indexB) {
				return findNode(nodes, id, sliceB, sliceA);
			} else {
				return this.selectNode(nodes, id, begin, end);
			}
		}
	}

	/**
	 * 选择节点，选中的节点如果没有使用过标记为使用中
	 */
	private List<NodeSession> selectNode(final List<NodeSession> nodes, final byte[] id, final int begin, final int end) {
		Stream<NodeSession> select;
		if(begin < end) {
			select = nodes.stream().skip(begin).limit(end - begin);
		} else {
			select = Stream.concat(nodes.stream().limit(end), nodes.stream().skip(begin));
		}
		final Map<String, NodeSession> selectNodes = select
			.collect(Collectors.toMap(node -> {
				return xor(node.getId(), id);
			}, node -> {
				return node;
			}));
		return selectNodes.entrySet().stream()
			.sorted((a, b) -> {
				return a.getKey().compareTo(b.getKey());
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
	 * 设置token，如果对应nodeId不存在，这加入网络
	 */
	public void token(byte[] nodeId, Request request, byte[] token) {
		final InetSocketAddress address = request.getAddress();
		synchronized (this.nodes) {
			NodeSession old = select(nodeId);
			if(old == null) {
				old = newNodeSession(nodeId, address.getHostString(), address.getPort());
				this.sortNodes();
			}
			old.setToken(token);
		}
	}
	
	/**
	 * 标记可用节点：有相应的节点标记为可用节点
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
	 * 选择Node
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
	 * 异或运算，返回十六进制字符串
	 */
	public static final String xor(byte[] source, byte[] target) {
		final byte[] value = new byte[NODE_ID_LENGTH];
		for (int index = 0; index < NODE_ID_LENGTH; index++) {
			value[index] = (byte) (source[index] ^ target[index]);
		}
		return StringUtils.hex(value);
	}
	
	/**
	 * 获取不同的序号
	 */
	public static final int index(byte[] source, byte[] target) {
		for (int index = 0; index < NODE_ID_LENGTH; index++) {
			if(source[index] != target[index]) {
				return index;
			}
		}
		return NODE_ID_LENGTH;
	}

}
