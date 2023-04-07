package com.acgist.snail.config;

import com.acgist.snail.utils.EnumUtils;

public final class UtpConfig {

	public enum Type {
		DATA((byte) 0),
		FIN((byte) 1),
		STATE((byte) 2),
		RESET((byte) 3),
		SYN((byte) 4);

		private final byte type;
		private final byte typeVersion;

		private Type(byte type) {
			this.type = type;
			this.typeVersion = (byte) (type << 4 | VERSION);
		}

		public byte type() {
			return this.type;
		}

		public byte typeVersion() {
			return this.typeVersion;
		}

		public static final Type of(byte typeVersion) {
			final byte value = (byte) (typeVersion >> 4);
			if (value < 0 || value >= INDEX.length) {
				return null;
			}
			return INDEX[value];
		}
	}

	public static final byte VERSION = 1;
	public static final int HEADER_LENGTH = 20;
	public static final int HEADER_MIN_LENGTH = 20;
	public static final byte EXTENSION = 0;
	public static final int EXTENSION_MIN_LENGTH = 2;
	public static final int PACKET_MAX_LENGTH = 1452;
	public static final int WND_SIZE = SystemConfig.ONE_MB;
	public static final byte MAX_PUSH_TIMES = 3;
	public static final byte FAST_ACK_RETRY_TIMES = 3;

	private static final Type[] INDEX = EnumUtils.index(Type.class, Type::type);

	private UtpConfig() {}
}
