package com.sundy.ta.datasearch.proxy;

import lombok.Data;

@Data
public class Proxy {

	private String host;
	private int port;
	private String username;
	private String password;

	public Proxy(String host, int port) {
		this.host = host;
		this.port = port;
	}

	public Proxy(String host, int port, String username, String password) {
		this.host = host;
		this.port = port;
		this.username = username;
		this.password = password;
	}
	
}
