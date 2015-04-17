package com.googlecode.jmxtrans.elasticsearch;

import java.util.Map;

import com.googlecode.jmxtrans.cli.JmxTransConfiguration;

public class ElasticsearchProperties {

	public static final String DEFAULT_HOST = "localhost";
	public static final int DEFAULT_PORT = 9300;

	private String host;
	private int port;

	public ElasticsearchProperties(JmxTransConfiguration conf) {
		this.host = DEFAULT_HOST;
		this.port = DEFAULT_PORT;
		
		String elasticsearch = conf.getElasticsearchHost();
		if (elasticsearch != null) {
			String[] tmp = elasticsearch.split(":");
			this.host = tmp[0];
			this.port = Integer.parseInt(tmp[1]);
		}
	}

	public String getHost() {
		return host;
	}

	public int getPort() {
		return port;
	}

}
