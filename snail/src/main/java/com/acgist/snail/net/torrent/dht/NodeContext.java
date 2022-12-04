package com.acgist.snail.net.torrent.dht;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;
import java.util.zip.CRC32C;

import com.acgist.snail.config.DhtConfig;
import com.acgist.snail.config.SymbolConfig;
import com.acgist.snail.config.SystemConfig;
import com.acgist.snail.context.IContext;
import com.acgist.snail.logger.Logger;
import com.acgist.snail.logger.LoggerFactory;
import com.acgist.snail.utils.ArrayUtils;
import com.acgist.snail.utils.NetUtils;
import com.acgist.snail.utils.NumberUtils;
import com.acgist.snail.utils.StringUtils;

/**
 * DHT节点上下文
 * 协议链接（DHT）：https://baike.baidu.com/item/DHT
 * 协议链接（Kademlia）：https://baike.baidu.com/item/Kademlia
 * DHT Security extension
 * 协议链接：http://www.bittorrent.org/beps/bep_0042.html
 * 
 * BT协议使用DHT网络
 * eMule协议使用KAD网络
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
	 * Node查找时返回的列表长度：{@value}
	 */
	private static final int MAX_NODE_SIZE = 8;
	/**
	 * IPv4 MASK
	 */
	private static final byte[] IPV4_MASK = NumberUtils.intToBytes(0x030F3FFF);
	/**
	 * IPv6 MASK
	 */
	private static final byte[] IPV6_MASK = NumberUtils.longToBytes(0x0103070F1F3F7FFFL);
	
	/**
	 * NodeId
	 */
	private final byte[] nodeId;
	/**
	 * 节点列表
	 * 不要使用LinkedList：大量使用索引操作所以LinkedList性能很差
	 * 如果节点数量太多，建议使用分片查找算法，或者按照节点开始字母实现跳表。
	 */
	private final List<NodeSession> nodes;
	
	private NodeContext() {
		// 随机生成NodeId：拿到外网IP后再重新生成
		this.nodeId = ArrayUtils.random(DhtConfig.NODE_ID_LENGTH);
		this.nodes = new ArrayList<>();
		this.register();
	}
	
	/**
	 * @return NodeId
	 */
	public byte[] nodeId() {
		return this.nodeId;
	}
	
	/**
	 * 通过外网IP生成NodeId
	 * 
	 * @param ip 外网IP
	 * 
	 * @return NodeId
	 */
	public byte[] buildNodeId(String ip) {
		LOGGER.debug("生成NodeId：{}", ip);
		final byte[] mask;
		final byte[] ipBytes = NetUtils.ipToBytes(ip);
		if (ipBytes.length == SystemConfig.IPV4_LENGTH) {
			mask = IPV4_MASK;
		} else {
			mask = IPV6_MASK;
		}
		final int length = mask.length;
		for (int index = 0; index < length; ++index) {
			ipBytes[index] &= mask[index];
		}
		// 生成随机种子
		final Random random = NumberUtils.random();
		final byte rand = (byte) (random.nextInt());
		final byte r = (byte) (rand & 0x07);
		// 生成IP的循环冗余校验码
		final CRC32C crc32c = new CRC32C();
		ipBytes[0] |= r << 5;
		crc32c.update(ipBytes, 0, length);
		// 设置IP的循环冗余校验码
		final int crc = (int) crc32c.getValue();
		this.nodeId[0] = (byte) (crc >> 24);
		this.nodeId[1] = (byte) (crc >> 16);
		this.nodeId[2] = (byte) ((crc >> 8 & 0xF8) | (random.nextInt() & 0x07));
		// 随机填充3-18位置数据
		System.arraycopy(ArrayUtils.random(16), 0, this.nodeId, 3, 16);
		// 设置随机种子
		this.nodeId[19] = rand;
		return this.nodeId;
	}
	
	/**
	 * 注册默认DHT节点
	 */
	private void register() {
		final char colon = SymbolConfig.Symbol.COLON.toChar();
		DhtConfig.getInstance().nodes().forEach((nodeId, address) -> {
			final int index = address.lastIndexOf(colon);
			if(index > 0) {
				final String host = address.substring(0, index);
				final String port = address.substring(index + 1);
				if(StringUtils.isNotEmpty(host) && StringUtils.isNumeric(port)) {
					LOGGER.debug("注册默认DHT节点：{}-{}", nodeId, address);
					this.newNodeSession(StringUtils.unhex(nodeId), host, Integer.valueOf(port));
				} else {
					LOGGER.warn("注册默认DHT节点失败：{}-{}", nodeId, address);
				}
			} else {
				LOGGER.warn("注册默认DHT节点失败：{}-{}", nodeId, address);
			}
		});
	}
	
	/**
	 * @return 所有节点拷贝
	 */
	public List<NodeSession> nodes() {
		synchronized (this.nodes) {
			return new ArrayList<>(this.nodes);
		}
	}

	/**
	 * 整理节点长度
	 * 超过最大保存数量：删除验证节点和多余节点
	 * 
	 * @return 所有节点拷贝
	 * 
	 * @see #nodes()
	 */
	public List<NodeSession> resize() {
		synchronized (this.nodes) {
			final int oldSize = this.nodes.size();
			if(oldSize < DhtConfig.MAX_NODE_SIZE) {
				return this.nodes();
			}
			NodeSession session;
			final Random random = NumberUtils.random();
			final Iterator<NodeSession> iterator = this.nodes.iterator();
			while(iterator.hasNext()) {
				session = iterator.next();
				final boolean remove = switch (session.getStatus()) {
				// 随机均匀剔除
				case UNUSE -> random.nextInt(oldSize) >= DhtConfig.MAX_NODE_SIZE;
				// 验证状态剔除
				case VERIFY -> true;
				// 可用状态保存
				default -> false;
				};
				if(remove) {
					iterator.remove();
				}
			}
			if(LOGGER.isDebugEnabled()) {
				LOGGER.debug("整理节点长度：{}-{}", oldSize, this.nodes.size());
			}
			return this.nodes();
		}
	}
	
	/**
	 * 添加DHT节点
	 * 需要验证节点状态
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
			nodeSession.setStatus(NodeSession.Status.AVAILABLE);
		}
		return nodeSession;
	}
	
	/**
	 * 添加DHT节点
	 * 不用验证节点状态
	 * 
	 * @param nodeId 节点ID
	 * @param host 地址
	 * @param port 端口
	 * 
	 * @return DHT节点
	 */
	public NodeSession newNodeSession(byte[] nodeId, String host, Integer port) {
		if(nodeId.length != DhtConfig.NODE_ID_LENGTH) {
			LOGGER.warn("添加Node失败：{}-{}-{}", nodeId, host, port);
			return null;
		}
		synchronized (this.nodes) {
			final int[] nodeIndex = this.close(nodeId);
			final int index = nodeIndex[0];
			final int signum = nodeIndex[1];
			if(signum == 0) {
				return this.nodes.get(index);
			}
			final NodeSession nodeSession = NodeSession.newInstance(nodeId, host, port);
			LOGGER.debug("添加Node：{}", nodeSession);
			// 添加指定节点位置
			this.nodes.add(index, nodeSession);
			return nodeSession;
		}
	}
	
	/**
	 * 查找节点列表
	 * 
	 * @param target InfoHashHex或者NodeIdHex
	 * 
	 * @return 节点列表
	 */
	public List<NodeSession> findNode(String target) {
		return this.findNode(StringUtils.unhex(target));
	}
	
	/**
	 * 查找节点列表
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
				final int[] nodeIndex = this.close(target);
				final int index = nodeIndex[0];
				int size = 0;
				// 查找位置大于当前节点
				int leftPos = 1;
				int rightPos = 0;
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
					leftPos++;
					rightNode = this.select(index + rightPos, nodeSize);
					if(rightNode.useable()) {
						size++;
						closeNodes.add(rightNode);
					}
					rightPos++;
				}
			}
		}
		return closeNodes;
	}

	/**
	 * 标记节点为可用状态
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
	 * 选择节点
	 * 
	 * @param index 节点索引
	 * @param nodeSize 节点数量
	 * 
	 * @return 节点
	 */
	private NodeSession select(int index, int nodeSize) {
		if(index < 0) {
			index = index + nodeSize;
		} else if(index >= nodeSize) {
			index = index - nodeSize;
		}
		return this.nodes.get(index);
	}
	
	/**
	 * 选择节点
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
	 * 查找最近节点信息
	 * 
	 * 节点标记
	 * 正数=节点索引ID小于返回节点ID
	 * 0=节点索引ID等于返回节点ID
	 * 负数=节点索引ID大于返回节点ID
	 * 
	 * @param nodeId 节点ID
	 * 
	 * @return [ 节点索引, 节点标记 ]
	 */
	private int[] close(byte[] nodeId) {
		int index = 0;
		int signum = 1;
		NodeSession nodeSession;
		for (int jndex = 0; jndex < this.nodes.size(); jndex++) {
			nodeSession = this.nodes.get(jndex);
			signum = Arrays.compareUnsigned(nodeId, nodeSession.getId());
			if(signum > 0) {
				index = jndex + 1;
			} else {
				break;
			}
		}
		return new int[] {index, signum};
	}
	
}
