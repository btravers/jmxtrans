package com.googlecode.jmxtrans.model.output;

import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.annotation.concurrent.NotThreadSafe;

import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.conn.HttpClientConnectionManager;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.client.HttpClients;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.MoreObjects;
import com.google.common.collect.ImmutableList;
import com.google.inject.Inject;
import com.googlecode.jmxtrans.model.Query;
import com.googlecode.jmxtrans.model.Result;
import com.googlecode.jmxtrans.model.Server;
import com.googlecode.jmxtrans.model.ValidationException;
import com.googlecode.jmxtrans.model.naming.KeyUtils;

@NotThreadSafe
public class BluefloodWriter extends BaseOutputWriter {

	private static final Logger log = LoggerFactory.getLogger(BluefloodWriter.class);

	public static final String TTL = "ttl";

	public static final int DEFAULT_TTL = 2592000;
	public static final int DEFAULT_PORT = 19000;

	private final String host;
	private final Integer port;
	private final Integer ttl;

	private HttpClientBuilder clientBuilder;
	private HttpClientConnectionManager pool;

	@JsonCreator
	public BluefloodWriter(@JsonProperty("typeNames") ImmutableList<String> typeNames, @JsonProperty("booleanAsNumber") boolean booleanAsNumber,
			@JsonProperty("debug") Boolean debugEnabled, @JsonProperty("host") String host, @JsonProperty("port") Integer port,
			@JsonProperty("ttl") Integer ttl, @JsonProperty("settings") Map<String, Object> settings) {
		super(typeNames, booleanAsNumber, debugEnabled, settings);

		this.host = MoreObjects.firstNonNull(host, (String) getSettings().get(HOST));

		if (this.host == null) {
			throw new NullPointerException("Host cannot be null.");
		}

		this.port = MoreObjects.firstNonNull(port, Settings.getIntSetting(getSettings(), PORT, DEFAULT_PORT));

		this.ttl = MoreObjects.firstNonNull(ttl, Settings.getIntSetting(getSettings(), TTL, DEFAULT_TTL));
	}

	public Integer getPort() {
		return port;
	}

	public String getHost() {
		return host;
	}

	@Override
	public void validateSetup(Server server, Query query) throws ValidationException {
		// TODO Auto-generated method stub

	}

	@Override
	protected void internalWrite(Server server, Query query, ImmutableList<Result> results) throws Exception {
		String url = "http://" + host + ":" + port + "/v2.0/jmx/ingest";

		HttpClient httpClient = this.clientBuilder.build();
		HttpPost request = new HttpPost(url);

		String body = this.bodyRequest(server, query, results);
		StringEntity params = new StringEntity(body);
		request.addHeader("content-type", "application/x-www-form-urlencoded");
		request.setEntity(params);
		httpClient.execute(request);
	}
	
	public String bodyRequest(Server server, Query query, ImmutableList<Result> results) {
		String body = "[";

		for (Result result : results) {
			log.debug("Query result: {}", result);
			Map<String, Object> resultValues = result.getValues();
			if (resultValues != null) {
				for (Entry<String, Object> values : resultValues.entrySet()) {
					if (NumberUtils.isNumeric(values.getValue())) {
						String name = KeyUtils.getKeyString(server, query, result, values, getTypeNames(), null);
						String value = values.getValue().toString();
						long time = result.getEpoch();

						String line = "{ \"metricName\": \"" + name + "\", \"metricValue\": " + value + ", \"collectionTime\": " + time
								+ ", \"ttlInSeconds\": " + this.ttl + "},";
						log.debug("Blueflood Message: {}", line);
						body += line;

					} else {
						log.error("Unable to submit non-numeric value to Blueflood: [{}] from result [{}]", values.getValue(), result);
					}
				}
			}
		}

		if (body.length() > 1) {
			body = body.substring(0, body.length() - 1);
		}
		body += "]";
		
		return body;
	}

	@Inject
	public void setPool(HttpClientConnectionManager pool) {
		this.pool = pool;
		this.clientBuilder = HttpClients.custom().setConnectionManager(this.pool);
	}

	public static Builder builder() {
		return new Builder();
	}

	public static final class Builder {
		private final ImmutableList.Builder<String> typeNames = ImmutableList.builder();
		private boolean booleanAsNumber;
		private Boolean debugEnabled;
		private String host;
		private Integer port;
		private Integer ttl;

		private Builder() {
		}

		public Builder addTypeNames(List<String> typeNames) {
			this.typeNames.addAll(typeNames);
			return this;
		}

		public Builder addTypeName(String typeName) {
			typeNames.add(typeName);
			return this;
		}

		public Builder setBooleanAsNumber(boolean booleanAsNumber) {
			this.booleanAsNumber = booleanAsNumber;
			return this;
		}

		public Builder setDebugEnabled(boolean debugEnabled) {
			this.debugEnabled = debugEnabled;
			return this;
		}

		public Builder setHost(String host) {
			this.host = host;
			return this;
		}

		public Builder setPort(int port) {
			this.port = port;
			return this;
		}

		public Builder setTtl(int ttl) {
			this.ttl = ttl;
			return this;
		}

		public BluefloodWriter build() {
			return new BluefloodWriter(typeNames.build(), booleanAsNumber, debugEnabled, host, port, ttl, null);
		}
	}
}
