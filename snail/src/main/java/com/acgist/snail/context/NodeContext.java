package com.acgist.snail.context;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;
import java.util.zip.CRC32C;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.acgist.snail.IContext;
import com.acgist.snail.config.DhtConfig;
import com.acgist.snail.net.torrent.dht.DhtClient;
import com.acgist.snail.pojo.session.NodeSession;
import com.acgist.snail.utils.ArrayUtils;
import com.acgist.snail.utils.MapUtils;
import com.acgist.snail.utils.NetUtils;
import com.acgist.snail.utils.NumberUtils;
import com.acgist.snail.utils.StringUtils;

/**
 * <p>DHT节点上下文</p>
 * <p>协议链接（Kademlia）：https://baike.baidu.com/item/Kademlia</p>
 * <p>BT=DHT、eMule=KAD</p>
 * <p>DHT Security extension</p>
 * <p>协议链接：http://www.bittorrent.org/beps/bep_0042.html</p>
 * 
 * @author acgist
 */
public final class NodeContext implements IContext {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(NodeContext.class);
	
	private static final NodeContext INSTANCE = new NodeContext();
	
	public static final NodeContext getInstance() {
		return INSTANCE;
	}
	
	/**
	 * <p>Node查找时返回的Node列表长度：{@value}</p>
	 */
	private static final int MAX_NODE_SIZE = 8;
	/**
	 * <p>IPv4 MASK</p>
	 */
	private static final byte[] IPV4_MASK = NumberUtils.intToBytes(0x030F3FFF);
	/**
	 * <p>IPv6 MASK</p>
	 */
	private static final byte[] IPV6_MASK = NumberUtils.longToBytes(0x0103070F1F3F7FFFL);
	/**
	 * <p>NodeId随机开始位置：{@value}</p>
	 */
	private static final int NODE_ID_RANDOM_BEGIN = 3;
	/**
	 * <p>NodeId随机结束位置：{@value}</p>
	 */
	private static final int NODE_ID_RANDOM_END = 19;
	
	/**
	 * <p>当前客户端的NodeId</p>
	 */
	private final byte[] nodeId;
	/**
	 * <p>节点列表</p>
	 * <p>不要使用LinkedList，大量使用索引操作所以性能很差。</p>
	 * <p>如果节点数量很大建议使用其他数据结构</p>
	 */
	private final List<NodeSession> nodes;
	
	/**
	 * <p>禁止创建实例</p>
	 */
	private NodeContext() {
		this.nodeId = this.buildNodeId();
		this.nodes = new ArrayList<>();
		this.register();
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
	 * <p>生成系统NodeId</p>
	 * 
	 * @return 系统NodeId
	 */
	private byte[] buildNodeId() {
		return ArrayUtils.random(DhtConfig.NODE_ID_LENGTH);
	}

	/**
	 * <p>通过IP生成系统NodeId</p>
	 * 
	 * @param ip IP
	 * 
	 * @return NodeId
	 */
	public byte[] buildNodeId(String ip) {
		LOGGER.debug("生成系统NodeId：{}", ip);
		final byte[] mask;
		final byte[] ipBytes = NetUtils.ipToBytes(ip);
		if (ipBytes.length == NetUtils.IPV4_BYTES_LENGTH) {
			mask = IPV4_MASK;
		} else {
			mask = IPV6_MASK;
		}
		final int length = mask.length;
		for (int index = 0; index < length; ++index) {
			ipBytes[index] &= mask[index];
		}
		final CRC32C crc32c = new CRC32C();
		final Random random = NumberUtils.random();
		final byte rand = (byte) (random.nextInt() & 0xFF);
		final byte r = (byte) (rand & 0x07);
		ipBytes[0] |= r << 5;
		crc32c.update(ipBytes, 0, length);
		final int crc = (int) (crc32c.getValue() & 0xFFFFFFFF);
		this.nodeId[0] = (byte) ((crc >> 24) & 0xFF);
		this.nodeId[1] = (byte) ((crc >> 16) & 0xFF);
		this.nodeId[2] = (byte) (((crc >> 8) & 0xF8) | (random.nextInt() & 0x07));
		for (int index = NODE_ID_RANDOM_BEGIN; index < NODE_ID_RANDOM_END; ++index) {
			this.nodeId[index] = (byte) random.nextInt();
		}
		this.nodeId[19] = rand;
		return this.nodeId;
	}
	
	/**
	 * <p>注册DHT默认节点</p>
	 */
	private void register() {
		final var defaultNodes = DhtConfig.getInstance().nodes();
		if(MapUtils.isNotEmpty(defaultNodes)) {
			defaultNodes.forEach((nodeId, address) -> {
				LOGGER.debug("注册默认节点：{}-{}", nodeId, address);
				final int index = address.lastIndexOf(':');
				if(index != -1) {
					final String host = address.substring(0, index);
					final String port = address.substring(index + 1);
					if(StringUtils.isNotEmpty(host) && StringUtils.isNumeric(port)) {
						this.newNodeSession(StringUtils.unhex(nodeId), host, Integer.valueOf(port));
					}
				} else {
					LOGGER.warn("节点格式错误：{}-{}", nodeId, address);
				}
			});
		}
	}
	
	/**
	 * <p>获取所有节点拷贝</p>
	 * 
	 * @return 所有节点拷贝
	 */
	public List<NodeSession> nodes() {
		synchronized (this.nodes) {
			return new ArrayList<>(this.nodes);
		}
	}

	/**
	 * <p>整理节点长度</p>
	 * <p>超过最大保存数量：删除验证节点和多余节点</p>
	 * 
	 * @return 所有节点拷贝
	 */
	public List<NodeSession> resize() {
		synchronized (this.nodes) {
			final int size = this.nodes.size();
			if(size < DhtConfig.MAX_NODE_SIZE) {
				return this.nodes();
			}
			LOGGER.debug("整理节点长度：{}", size);
			final Random random = NumberUtils.random();
			final Iterator<NodeSession> iterator = this.nodes.iterator();
			NodeSession session;
			while(iterator.hasNext()) {
				session = iterator.next();
				switch (session.getStatus()) {
				case UNUSE:
					// 随机均匀剔除
					if(random.nextInt(size) >= DhtConfig.MAX_NODE_SIZE) {
						iterator.remove();
					}
					break;
				case VERIFY:
					iterator.remove();
					break;
				default:
					// 可用状态保存
					break;
				}
			}
			return this.nodes();
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
			// 标记可用
			nodeSession.setStatus(NodeSession.Status.AVAILABLE);
		}
		return nodeSession;
	}
	
	/**
	 * <p>添加DHT节点</p>
	 * <p>加入时不验证状态，使用时才验证。</p>
	 * 
	 * @param nodeId 节点ID
	 * @param host 地址
	 * @param port 端口
	 * 
	 * @return DHT节点
	 */
	public NodeSession newNodeSession(byte[] nodeId, String host, Integer port) {
		synchronized (this.nodes) {
			final int[] nodeIndex = this.selectNode(nodeId);
			final int index = nodeIndex[0];
			final int signum = nodeIndex[1];
			// 完全匹配
			if(signum == 0) {
				return this.nodes.get(index);
			}
			final NodeSession nodeSession = NodeSession.newInstance(nodeId, host, port);
			if(nodeSession.getId().length == DhtConfig.NODE_ID_LENGTH) {
				if(LOGGER.isDebugEnabled()) {
					LOGGER.debug("添加Node：{}-{}-{}", StringUtils.hex(nodeId), nodeSession.getHost(), nodeSession.getPort());
				}
				// 添加指定节点位置
				this.nodes.add(index, nodeSession);
			}
			return nodeSession;
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
	 * <p>如果节点数据很大，可以优化算法，分片进行查询。</p>
	 * 
	 * @param target InfoHash或者NodeId
	 * 
	 * @return 节点列表
	 */
	public List<NodeSession> findNode(byte[] target) {
		List<NodeSession> closeNodes;
		synchronized (this.nodes) {
			final int nodeSize = this.nodes.size();
			if(nodeSize <= MAX_NODE_SIZE) {
				closeNodes = this.nodes.stream()
					.filter(NodeSession::useable)
					.collect(Collectors.toList());
			} else {
				closeNodes = new ArrayList<>();
				final int[] nodeIndex = this.selectNode(target);
				final int index = nodeIndex[0];
				int size = 0;
				int leftPos = 0;
				int rightPos = 1;
				NodeSession leftNode;
				NodeSession rightNode;
				while(
					// 指定数量结果
					size < MAX_NODE_SIZE &&
					// 轮询整个列表
					leftPos + rightPos < nodeSize
				) {
					leftNode = this.select(index - leftPos, nodeSize);
					if(leftNode.useable()) {
						size++;
						// 前面添加防止乱序
						closeNodes.add(0, leftNode);
					}
					rightNode = this.select(index + rightPos, nodeSize);
					if(rightNode.useable()) {
						size++;
						closeNodes.add(rightNode);
					}
					leftPos++;
					rightPos++;
				}
			}
		}
		return closeNodes;
	}

	/**
	 * <p>标记节点为可用状态</p>
	 * 
	 * @param nodeId 节点ID
	 */
	public void available(byte[] nodeId) {
		synchronized (this.nodes) {
			final NodeSession node = this.select(nodeId);
			if(node != null) {
				node.setStatus(NodeSession.Status.AVAILABLE);
			}
		}
	}

	/**
	 * <p>选择节点</p>
	 * 
	 * @param index 节点索引
	 * @param nodeSize 节点数量
	 * 
	 * @return 节点
	 */
	private NodeSession select(int index, int nodeSize) {
		if(index < 0) {
			index = nodeSize + index;
		} else if(index >= nodeSize) {
			index = index - nodeSize;
		}
		return this.nodes.get(index);
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
			if(Arrays.equals(nodeId, nodeSession.getId())) {
				return nodeSession;
			}
		}
		return null;
	}
	
	/**
	 * <p>查找最近节点信息</p>
	 * <p>节点索引：最近节点索引</p>
	 * <p>节点标记：等于零时表示完全匹配</p>
	 * 
	 * @param nodeId 节点ID
	 * 
	 * @return 最近节点信息：节点索引、节点标记
	 */
	private int[] selectNode(byte[] nodeId) {
		int index = 0;
		int signum = 1;
		NodeSession nodeSession;
		for (int jndex = 0; jndex < this.nodes.size(); jndex++) {
			nodeSession = this.nodes.get(jndex);
			signum = Arrays.compareUnsigned(nodeId, nodeSession.getId());
			if(signum > 0) {
				// 尾部添加
				index = jndex + 1;
			} else {
				break;
			}
		}
		return new int[] {index, signum};
	}
	
}
