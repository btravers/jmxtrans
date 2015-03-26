package com.googlecode.jmxtrans.model.output;

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

}
