package com.googlecode.jmxtrans.elasticsearch;

import java.io.IOException;
import java.util.Observable;

import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.Client;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.transport.InetSocketTransportAddress;
import org.elasticsearch.search.SearchHit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.guava.GuavaModule;
import com.google.common.collect.ImmutableList;
import com.googlecode.jmxtrans.cli.JmxTransConfiguration;
import com.googlecode.jmxtrans.model.JmxProcess;
import com.googlecode.jmxtrans.model.Server;

public class ElasticsearchClient extends Observable {

	private static final Logger log = LoggerFactory.getLogger(ElasticsearchClient.class);

	public final static String INDEX = ".jmxtrans";
	public final static String TYPE = "conf";

	private Client client;
	private ElasticsearchProperties properties;

	enum EVENT {
		DELETE, UPDATE, ADD
	}

	public ElasticsearchClient(JmxTransConfiguration conf) {
		this.properties = new ElasticsearchProperties(conf);

		log.info("ElasticsearchProperties: [" + this.properties.getHost() + ":" + this.properties.getPort() + "]");

		this.client = new TransportClient().addTransportAddress(new InetSocketTransportAddress(this.properties.getHost(), this.properties.getPort()));
	}

	public ImmutableList<Server> getAll() throws JsonParseException, JsonMappingException, IOException {
		SearchResponse response = this.client.prepareSearch(INDEX).setTypes(TYPE).execute().actionGet();

		ObjectMapper mapper = new ObjectMapper();
		mapper.registerModule(new GuavaModule());

		ImmutableList.Builder<Server> builder = ImmutableList.builder();
		for (SearchHit hit : response.getHits().getHits()) {
			JmxProcess jmx = mapper.readValue(hit.getSourceAsString(), JmxProcess.class);
			builder.addAll(jmx.getServers());
		}

		return builder.build();
	}

	public void stopElasticsearchClient() throws IOException {
		this.client.close();
	}

}
