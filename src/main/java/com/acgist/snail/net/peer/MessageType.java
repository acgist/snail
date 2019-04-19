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
		ut_pex,
		ut_metadata;
	}
	
	/**
	 * 注意顺序
	 * UtMetadata消息类型
	 */
	public enum UtMetadataType {
		request,
		data,
		reject;
	}

}
