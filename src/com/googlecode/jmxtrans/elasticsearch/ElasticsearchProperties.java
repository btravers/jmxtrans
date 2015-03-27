package com.googlecode.jmxtrans.elasticsearch;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import com.googlecode.jmxtrans.cli.JmxTransConfiguration;

public class ElasticsearchProperties {

	public static final String DEFAULT_HOST = "localhost";
	public static final int DEFAULT_PORT = 9300;
	public static final String DEFAULT_CLUSTER = "elasticsearch";
	public static final String DEFAULT_PROPERTIES8FILE_NAME = "elasticsearch.properties";

	private String propFileName;
	private Properties properties;

	private String host;
	private int port;
	private String cluster;

	public ElasticsearchProperties(JmxTransConfiguration config) {
		if (config.getElasticsearchPropertiesFile() == null) {
			this.propFileName = DEFAULT_PROPERTIES8FILE_NAME;

		} else {
			this.propFileName = config.getElasticsearchPropertiesFile();
		}

		this.host = DEFAULT_HOST;
		this.port = DEFAULT_PORT;
		this.cluster = DEFAULT_CLUSTER;
	}

	public void loadProperties() throws IOException {
		this.properties = new Properties();
		InputStream inputStream = getClass().getClassLoader().getResourceAsStream(propFileName);
		if (inputStream != null) {
			this.properties.load(inputStream);

			this.host = this.properties.getProperty("HOST");
			this.port =	Integer.parseInt(this.properties.getProperty("PORT"));
			this.cluster = this.properties.getProperty("CLUSTER");
		}
	}

	public String getHost() {
		return host;
	}

	public int getPort() {
		return port;
	}

	public String getCluster() {
		return cluster;
	}

}
