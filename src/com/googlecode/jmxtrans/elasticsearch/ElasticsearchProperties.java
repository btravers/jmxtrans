package com.googlecode.jmxtrans.elasticsearch;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import com.googlecode.jmxtrans.cli.JmxTransConfiguration;

public class ElasticsearchProperties {

	public static final String DEFAULT_HOST = "localhost";
	public static final int DEFAULT_PORT = 9300;
	public static final String DEFAULT_CLUSTER = "elasticsearch";
	public static final String DEFAULT_PROPERTIES_FILE_NAME = "elasticsearch.properties";

	private File propFile;
	private Properties properties;

	private String host;
	private int port;
	private String cluster;

	public ElasticsearchProperties(JmxTransConfiguration config) {
		this.propFile = config.getElasticsearchPropertiesFile();
		
		this.host = DEFAULT_HOST;
		this.port = DEFAULT_PORT;
		this.cluster = DEFAULT_CLUSTER;
	}

	public void loadProperties() throws IOException {
		this.properties = new Properties();
		
		if (this.propFile == null) {
			this.propFile = new File(DEFAULT_PROPERTIES_FILE_NAME);
		}
		
		if (!this.propFile.exists() || this.propFile.isDirectory()) {
			return;
		}
		
		InputStream inputStream = new FileInputStream(this.propFile);
		if (inputStream != null) {
			this.properties.load(inputStream);

			if (this.properties.getProperty("HOST") != null) {
				this.host = this.properties.getProperty("HOST");
			}
			if (this.properties.getProperty("PORT") != null) {
				this.port =	Integer.parseInt(this.properties.getProperty("PORT"));
			}
			if (this.properties.getProperty("CLUSTER") != null) {
				this.cluster = this.properties.getProperty("CLUSTER");
			}
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
