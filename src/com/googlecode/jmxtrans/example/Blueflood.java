package com.googlecode.jmxtrans.example;

import com.googlecode.jmxtrans.model.JmxProcess;
import com.googlecode.jmxtrans.model.Query;
import com.googlecode.jmxtrans.model.Server;
import com.googlecode.jmxtrans.model.output.BluefloodWriter;
import com.googlecode.jmxtrans.util.JsonPrinter;

public class Blueflood {
	private static JsonPrinter printer = new JsonPrinter(System.out);

	public static void main(String[] args) throws Exception {
		printer.prettyPrint(new JmxProcess(Server
				.builder()
				.setHost("192.168.33.10")
				.setPort("9991")
				.addQuery(
						Query.builder().setObj("java.lang:type=GarbageCollector,name=ConcurrentMarkSweep")
								.addOutputWriter(BluefloodWriter.builder().setHost("192.168.33.12").setPort(19000).setDebugEnabled(true).build())
								.build()).build()));
	}
}
