package com.acgist.snail.net.peer;

/**
 * 消息类型
 */
public class MessageType {

	/**
	 * Peer消息类型
	 */
	public enum Type {
		
		choke((byte) 0),
		unchoke((byte) 1),
		interested((byte) 2),
		notInterested((byte) 3),
		have((byte) 4),
		bitfield((byte) 5),
		request((byte) 6),
		piece((byte) 7),
		cancel((byte) 8),
		port((byte) 9),
		extension((byte) 20);
		
		Type(byte value) {
			this.value = value;
		}
		
		private byte value;
		
		public byte value() {
			return this.value;
		}
		
		public static final Type valueOf(byte value) {
			Type[] types = Type.values();
			for (Type type : types) {
				if(type.value() == value) {
					return type;
				}
			}
			return null;
		}
		
	}
	
	/**
	 * 扩展消息类型
	 */
	public enum ExtensionType {
		
		handshake((byte) 0, false), // 默认
		ut_pex((byte) 1, false),
		ut_metadata((byte) 2, true);
		
		ExtensionType(byte value, boolean support) {
			this.value = value;
			this.support = support;
		}

		private byte value;
		private boolean support;
		
		public byte value() {
			return this.value;
		}
		
		public boolean support() {
			return this.support;
		}
		
		public static final ExtensionType valueOf(byte value) {
			ExtensionType[] types = ExtensionType.values();
			for (ExtensionType type : types) {
				if(type.value() == value) {
					return type;
				}
			}
			return null;
		}
		
		public static final ExtensionType valueOfName(String name) {
			final ExtensionType[] types = ExtensionType.values();
			for (ExtensionType type : types) {
				if(type.name().equals(name)) {
					return type;
				}
			}
			return null;
		}
		
	}
	
	/**
	 * UtMetadata消息类型
	 */
	public enum UtMetadataType {
		
		request((byte) 0),
		data((byte) 1),
		reject((byte) 2);
		
		UtMetadataType(byte value) {
			this.value = value;
		}
		
		private byte value;
		
		public byte value() {
			return this.value;
		}
		
		public static final UtMetadataType valueOf(byte value) {
			UtMetadataType[] types = UtMetadataType.values();
			for (UtMetadataType type : types) {
				if(type.value() == value) {
					return type;
				}
			}
			return null;
		}
		
	}
	
	/**
	 * PeerClient动作
	 */
	public enum Action {
		download, // 下载文件
		torrent; // 下载种子
	}

}
