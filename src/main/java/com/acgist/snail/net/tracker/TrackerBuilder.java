package com.acgist.snail.net.tracker;

public class TrackerBuilder {

	public enum EventType {
		QUERY(0), COMPLETE(1), START(2), STOP(3);
		private int code;
		EventType(int code) {
			this.code = code;
		}
		public int code() {
			return code;
		}
	}
	
}
