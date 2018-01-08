package com.sundy.ta.datasearch.constant;

public interface Constant {

	public static enum StatusCode {
		OK(200);
		
		private final Integer code;
		
		private StatusCode(Integer code) {
			this.code = code;
		}
		
		public Integer getCode() {
			return code;
		}
	}
	
	public static enum Method {
		GET("GET"), HEAD("HEAD"), POST("POST"),
		PUT("PUT"), DELETE("DELETE"), TRACE("TRACE"), 
		CONNECT("CONNECT");
		
		private final String code;

		private Method(String code) {
			this.code = code;
		}

		public String getCode() {
			return code;
		}
	}
	
	public static enum Header {
		REFERER("Referer"), USER_AGENT("User-Agent");
		
		private final String code;

		private Header(String code) {
			this.code = code;
		}

		public String getCode() {
			return code;
		}
	}
	
	
	public static enum State {
		INIT(1), RUNNING(2), STOP(3);
		
		private final int code;

		private State(int code) {
			this.code = code;
		}

		public int getCode() {
			return code;
		}
	}
	
	
	
}
