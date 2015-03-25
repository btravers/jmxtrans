package com.googlecode.jmxtrans.model.output;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Scanner;

import javax.annotation.concurrent.NotThreadSafe;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.MoreObjects;
import com.google.common.collect.ImmutableList;
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

	@JsonCreator
	public BluefloodWriter(@JsonProperty("typeNames") ImmutableList<String> typeNames, @JsonProperty("booleanAsNumber") boolean booleanAsNumber,
			@JsonProperty("debug") Boolean debugEnabled, @JsonProperty("host") String host, @JsonProperty("port") Integer port,
			@JsonProperty("ttl") int ttl, @JsonProperty("settings") Map<String, Object> settings) {
		super(typeNames, booleanAsNumber, debugEnabled, settings);

		this.host = MoreObjects.firstNonNull(host, (String) getSettings().get(HOST));
		this.port = MoreObjects.firstNonNull(port, Settings.getIntSetting(getSettings(), PORT, DEFAULT_PORT));

		if (this.host == null) {
			throw new NullPointerException("Host cannot be null.");
		}

		if (this.port == null) {
			throw new NullPointerException("Port cannot be null.");
		}

		// this.ttl = MoreObjects.firstNonNull(ttl,
		// Settings.getIntSetting(getSettings(), TTL, DEFAULT_TTL));
		this.ttl = DEFAULT_TTL;
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
		// TODO Auto-generated method stub
		String url = "http://" + host + ":" + port + "/v2.0/jmx/ingest";

		URL bluefloodServer = null;
		BufferedWriter writer = null;

		try {
			bluefloodServer = new URL(url);

			HttpURLConnection connection = (HttpURLConnection) bluefloodServer.openConnection();
			connection.setDoOutput(true);
			connection.setRequestMethod("POST");
			writer = new BufferedWriter(new OutputStreamWriter(connection.getOutputStream()));

			String body = "[";

			for (Result result : results) {
				log.info("Query result: {}", result);
				Map<String, Object> resultValues = result.getValues();
				if (resultValues != null) {
					for (Entry<String, Object> values : resultValues.entrySet()) {
						if (NumberUtils.isNumeric(values.getValue())) {
							String name = KeyUtils.getKeyString(query, result, values, getTypeNames());
							String value = values.getValue().toString();
							long time = result.getEpoch();

							String line = "{ \"metricName\": \"" + name + "\", \"metricValue\": " + value + ", \"collectionTime\": " + time
									+ ", \"ttlInSeconds\": " + this.ttl + "},";
							log.info("Blueflood Message: {}", line);
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
			writer.write(body);
			writer.close();

			Scanner scanner = new Scanner(connection.getInputStream());
			while (scanner.hasNextLine()) {
				log.debug(scanner.nextLine());
			}
			scanner.close();
		} catch (MalformedURLException e) {
			log.error(e.getMessage());
		} catch (IOException e) {
			log.error(e.getMessage());
		} finally {
			if (writer != null) {
				writer.close();
			}
		}
	}

}
