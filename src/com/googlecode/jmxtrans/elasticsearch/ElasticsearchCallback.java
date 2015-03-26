package com.googlecode.jmxtrans.elasticsearch;

public interface ElasticsearchCallback {
	void serverModified(int id) throws Exception;

	void serverDeleted(int id) throws Exception;

	void serverAdded(int id) throws Exception;
}
