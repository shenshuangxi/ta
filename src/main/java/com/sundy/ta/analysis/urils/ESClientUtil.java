package com.sundy.ta.analysis.urils;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.apache.http.HttpEntity;
import org.apache.http.HttpHost;
import org.apache.http.entity.ContentType;
import org.apache.http.nio.entity.NStringEntity;
import org.apache.http.util.EntityUtils;
import org.elasticsearch.client.Response;
import org.elasticsearch.client.RestClient;

public class ESClientUtil {

	public static void main(String[] args) throws IOException {
		RestClient restClient = RestClient.builder(new HttpHost("192.168.137.4", 10100, "http")).build();
		Response response = restClient.performRequest("GET", "/");
		System.out.println(EntityUtils.toString(response.getEntity()));
		
//		Map<String, String> params = new HashMap<String, String>();
//		String jsonString = "{" +
//		            "\"user\":\"kimchy\"," +
//		            "\"postDate\":\"2013-01-30\"," +
//		            "\"message\":\"trying out Elasticsearch\"" +
//		        "}";
//		HttpEntity entity = new NStringEntity(jsonString, ContentType.APPLICATION_JSON);
//		Response response = restClient.performRequest("PUT", "/posts/doc/1", params, entity);
		
	}
	
}
