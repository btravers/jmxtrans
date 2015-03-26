package com.googlecode.jmxtrans.elasticsearch;

import java.io.IOException;
import java.util.Observable;

import org.elasticsearch.action.get.GetResponse;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.action.update.UpdateRequest;
import org.elasticsearch.node.Node;
import org.elasticsearch.node.NodeBuilder;
import org.elasticsearch.search.SearchHit;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.guava.GuavaModule;
import com.google.common.collect.ImmutableList;
import com.googlecode.jmxtrans.model.JmxProcess;
import com.googlecode.jmxtrans.model.Server;

public class ElasticsearchClient extends Observable {
	
	public final static String INDEX = ".jmxtrans";
	public final static String TYPE = "conf";

	private Node node;
	private int id;
	private EVENT event;
	
	enum EVENT {
		DELETE, UPDATE, ADD
	}


	public ElasticsearchClient() {
		this.node = NodeBuilder.nodeBuilder().client(true).node();
	}

	public int getId() {
		return id;
	}
	
	public EVENT getEvent() {
		return this.event;
	}

	public void index(Server server) throws JsonProcessingException {
		ObjectMapper mapper = new ObjectMapper();
		mapper.getSerializationConfig().without(SerializationFeature.WRITE_NULL_MAP_VALUES);

		String json = mapper.writeValueAsString(new JmxProcess(server));

		IndexResponse response = this.node.client().prepareIndex(INDEX, TYPE).setSource(json).execute().actionGet();

		this.id = Integer.parseInt(response.getId());
		this.event = EVENT.ADD;
		setChanged();
		notifyObservers();
	}

	public ImmutableList<Server> getAll() throws JsonParseException, JsonMappingException, IOException {
		SearchResponse response = this.node.client().prepareSearch(INDEX).setTypes(TYPE).execute().actionGet();

		ObjectMapper mapper = new ObjectMapper();
		mapper.registerModule(new GuavaModule());

		ImmutableList.Builder<Server> builder = ImmutableList.builder();
		for (SearchHit hit : response.getHits().getHits()) {
			JmxProcess jmx = mapper.readValue(hit.getSourceAsString(), JmxProcess.class);
			builder.addAll(jmx.getServers());
		}

		return builder.build();
	}

	public Server get(String id) throws JsonParseException, JsonMappingException, IOException {
		GetResponse response = this.node.client().prepareGet(INDEX, TYPE, id).execute().actionGet();

		ObjectMapper mapper = new ObjectMapper();
		mapper.registerModule(new GuavaModule());
		JmxProcess jmx = mapper.readValue(response.getSourceAsString(), JmxProcess.class);

		return jmx.getServers().get(0);
	}

	public void update(Server server, String id) throws JsonProcessingException {
		ObjectMapper mapper = new ObjectMapper();
		mapper.getSerializationConfig().without(SerializationFeature.WRITE_NULL_MAP_VALUES);

		String json = mapper.writeValueAsString(new JmxProcess(server));

		UpdateRequest updateRequest = new UpdateRequest(INDEX, TYPE, id).doc(json);
		this.node.client().update(updateRequest);

		this.id = Integer.parseInt(id);
		this.event = EVENT.UPDATE;
		setChanged();
		notifyObservers();
	}

	public void delete(Server server, String id) {
		this.node.client().prepareDelete(INDEX, TYPE, id).execute().actionGet();

		this.id = Integer.parseInt(id);
		this.event = EVENT.DELETE;
		setChanged();
		notifyObservers();
	}

	public void stopElasticsearchClient() throws IOException {
		this.node.close();
	}

}
