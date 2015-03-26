package com.googlecode.jmxtrans.elasticsearch;

import java.util.Observable;
import java.util.Observer;

public class ElasticsearchObserver implements Observer {

	private final ElasticsearchCallback callback;

	public ElasticsearchObserver(ElasticsearchCallback callback) {
		this.callback = callback;
	}

	void init(ElasticsearchClient ec) {
		ec.addObserver(this);
	}

	@Override
	public void update(Observable o, Object arg) {
		if (o instanceof ElasticsearchClient) {
			ElasticsearchClient client = (ElasticsearchClient) o;
			switch (client.getEvent()) {
			case ADD:
				try {
					this.callback.serverAdded(client.getId());
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				break;
			case DELETE:
				try {
					this.callback.serverDeleted(client.getId());
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				break;
			case UPDATE:
				try {
					this.callback.serverModified(client.getId());
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				break;
			}
		}
	}

}
