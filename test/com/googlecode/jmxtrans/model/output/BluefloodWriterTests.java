package com.googlecode.jmxtrans.model.output;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.googlecode.jmxtrans.model.Query;
import com.googlecode.jmxtrans.model.Result;
import com.googlecode.jmxtrans.model.Server;
import com.googlecode.jmxtrans.model.ValidationException;

import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class BluefloodWriterTests {

	@Test(expected = NullPointerException.class)
	public void settingsHost() throws ValidationException {
		BluefloodWriter writter = BluefloodWriter.builder().setPort(123).build();
	}

	@Test
	public void defaultPort() throws ValidationException {
		BluefloodWriter writter = BluefloodWriter.builder().setHost("localhost").build();
		assertThat(writter.getPort()).isEqualTo(new Integer(19000));
	}

	@Test
	public void writeSingleResult() throws Exception {
		Server server = Server.builder().setHost("host").setPort("123").build();
		Query query = Query.builder().build();
		long timestamp = System.currentTimeMillis();
		Result result = new Result(timestamp, "attributeName", "className", "classNameAlias", "typeName", ImmutableMap.of("key", (Object) 1));

		BluefloodWriter writer = BluefloodWriter.builder().setHost("localhost").setPort(2003).build();

		assertThat(writer.bodyRequest(server, query, ImmutableList.of(result)))
				.isEqualTo(
						"[{ \"metricName\": \"host_123.classNameAlias.attributeName_key\", \"metricValue\": 1, \"collectionTime\": " + timestamp + ", \"ttlInSeconds\": 2592000}]");
	}
}
