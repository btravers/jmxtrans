package com.googlecode.jmxtrans.elasticsearch;

import java.io.IOException;
import java.util.Observable;

import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.Client;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.transport.InetSocketTransportAddress;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.bucket.terms.Terms;
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
	private long timestamp;
	private int size;

	enum EVENT {
		DELETE, UPDATE, ADD
	}

	public ElasticsearchClient(JmxTransConfiguration conf) {
		this.properties = new ElasticsearchProperties(conf);

		log.info("ElasticsearchProperties: [" + this.properties.getHost() + ":" + this.properties.getPort() + "]");

		this.client = new TransportClient().addTransportAddress(new InetSocketTransportAddress(this.properties.getHost(), this.properties.getPort()));
		
		// Initilization of timestamp and size using reloadConf method
		this.reloadConf();
	}

	public ImmutableList<Server> getAll() throws JsonParseException, JsonMappingException, IOException {
		SearchResponse response = this.client.prepareSearch(INDEX).setTypes(TYPE).setSize(Integer.MAX_VALUE).execute().actionGet();

		ObjectMapper mapper = new ObjectMapper();
		mapper.registerModule(new GuavaModule());

		ImmutableList.Builder<Server> builder = ImmutableList.builder();
		for (SearchHit hit : response.getHits().getHits()) {
			JmxProcess jmx = mapper.readValue(hit.getSourceAsString(), JmxProcess.class);
			builder.addAll(jmx.getServers());
		}

		return builder.build();
	}

	public boolean reloadConf() {
		SearchResponse response = this.client.prepareSearch(INDEX).setTypes(TYPE).setQuery(QueryBuilders.matchAllQuery())
				.addAggregation(AggregationBuilders.terms("agg").field("_timestamp").size(0).order(Terms.Order.term(false))).execute().actionGet();

		Terms agg = response.getAggregations().get("agg");

		if (agg.getBuckets().size() == 0) {
			if (this.size == 0) {
				return false;
			}
			this.size = 0;
			return true;
		}
		
		if (agg.getBuckets().size() != this.size) {
			this.size = agg.getBuckets().size();
			this.timestamp = Long.parseLong(agg.getBuckets().get(0).getKey());
			return true;
		}
		
		if (this.timestamp < Long.parseLong(agg.getBuckets().get(0).getKey())) {
			this.timestamp = Long.parseLong(agg.getBuckets().get(0).getKey());
			return true;
		}

		return false;
	}

	public void stopElasticsearchClient() throws IOException {
		this.client.close();
	}

}
