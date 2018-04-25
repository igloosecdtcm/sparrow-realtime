/*package com.igloosec.valkyrie.client;

import java.io.IOException;

import org.apache.http.HttpHost;
import org.elasticsearch.ElasticsearchException;
import org.elasticsearch.action.get.GetRequest;
import org.elasticsearch.action.get.GetResponse;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.rest.RestStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.igloosec.realtime.ValkyrieClientTask;

public class ElasticsearchClient implements ValkyrieClient {
	private final Logger logger =  LoggerFactory.getLogger(ElasticsearchClient.class);
	RestHighLevelClient client = null;
	
	public ElasticsearchClient(String ip, int port) {
		client = new RestHighLevelClient(RestClient.builder(new HttpHost(ip, port, "http")));
	}
	
	public String get(String idx, String doc, String id) {
		GetRequest getRequest = new GetRequest(idx, doc, id);
		try {
		    GetResponse getResponse = client.get(getRequest);
		    logger.info(getResponse.getId());
		} catch (ElasticsearchException e) {
		    if (e.status() == RestStatus.NOT_FOUND) {
			    logger.info("exception - Not Found");
		    }
		} catch (IOException e) {
			e.printStackTrace();
		}
		return "s";
	}
	public void stop (){
		try {
			client.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
*/