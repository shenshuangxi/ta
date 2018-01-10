package com.sundy.ta.analysis.urils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.http.HttpEntity;
import org.apache.http.HttpHost;
import org.apache.http.entity.ContentType;
import org.apache.http.nio.entity.NStringEntity;
import org.apache.http.util.EntityUtils;
import org.elasticsearch.client.Response;
import org.elasticsearch.client.RestClient;

import com.alibaba.fastjson.JSON;

public class ESClientUtil {

	public static void main(String[] args) throws IOException {
		RestClient restClient = RestClient.builder(new HttpHost("192.168.137.4", 10100, "http")).build();
//		Response response = restClient.performRequest("GET", "/");
		
		Map<String, String> params = new HashMap<String, String>();
		Map<String, Object> jsonMap = new HashMap<String, Object>();
		jsonMap.put("first_name", "John");
		jsonMap.put("last_name", "Smith");
		jsonMap.put("age", 25);
		jsonMap.put("about", "I	love to	go rock	climbing");
		List<String> interests= new ArrayList<String>();
		interests.add("sports");
		interests.add("music");
		jsonMap.put("interests", interests);
		String jsonString = JSON.toJSONString(jsonMap);
		HttpEntity entity = new NStringEntity(jsonString, ContentType.APPLICATION_JSON);
		Response response = restClient.performRequest("PUT", "/megacorp/employee/1", params, entity);
		System.out.println(EntityUtils.toString(response.getEntity()));
		EntityUtils.consume(entity);
		
		response = restClient.performRequest("GET", "/megacorp/employee/_search?pretty");
		System.out.println(EntityUtils.toString(response.getEntity()));
		EntityUtils.consume(entity);
		
		restClient.close();
		
		
		
//		String jsonString = "{" +
//		            "\"user\":\"kimchy\"," +
//		            "\"postDate\":\"2013-01-30\"," +
//		            "\"message\":\"trying out Elasticsearch\"" +
//		        "}";
//		HttpEntity entity = new NStringEntity(jsonString, ContentType.APPLICATION_JSON);
		
	}
	
}
