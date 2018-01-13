package com.sundy.ta;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.http.HttpHost;
import org.apache.http.nio.util.SharedInputBuffer;
import org.apache.lucene.search.Explanation;
import org.elasticsearch.Build;
import org.elasticsearch.Version;
import org.elasticsearch.action.ActionListener;
import org.elasticsearch.action.DocWriteResponse;
import org.elasticsearch.action.get.GetRequest;
import org.elasticsearch.action.get.GetResponse;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.action.main.MainResponse;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.action.search.SearchResponse.Clusters;
import org.elasticsearch.action.search.ShardSearchFailure;
import org.elasticsearch.action.support.replication.ReplicationResponse.ShardInfo;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.cluster.ClusterName;
import org.elasticsearch.common.document.DocumentField;
import org.elasticsearch.common.unit.TimeValue;
import org.elasticsearch.common.xcontent.XContentType;
import org.elasticsearch.index.engine.Engine.Result;
import org.elasticsearch.index.query.MatchAllQueryBuilder;
import org.elasticsearch.index.query.Operator;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.index.shard.ShardId;
import org.elasticsearch.rest.RestStatus;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHit.NestedIdentity;
import org.elasticsearch.search.SearchHits;
import org.elasticsearch.search.SearchShardTarget;
import org.elasticsearch.search.aggregations.Aggregation;
import org.elasticsearch.search.aggregations.Aggregations;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightField;
import org.elasticsearch.search.profile.ProfileShardResult;
import org.elasticsearch.search.suggest.Suggest;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.alibaba.fastjson.JSON;

public class ESTest {

	private RestHighLevelClient client = null;
	
	@Before
	public void before() {
		client = new RestHighLevelClient(RestClient.builder(new HttpHost("192.168.137.4", 10100, "http")));
	}
	
	@After
	public void after() {
		if(client!=null) {
			try {
				client.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
	
	@Test
	public void put() throws IOException {
		
		MainResponse mainResponse = client.info();
		Build build = mainResponse.getBuild();
		String date = build.date();
		boolean isSnapshot = build.isSnapshot();
		String shotHash = build.shortHash();
		System.out.println("build: [ date="+date+" isSnapshot="+isSnapshot+" shotHash="+shotHash+"]");
		
		ClusterName clusterName = mainResponse.getClusterName();
		System.out.println("clusterName=" + clusterName);
		
		String clusterUUID = mainResponse.getClusterUuid();
		System.out.println("clusterUUID="+clusterUUID);
		String nodeName = mainResponse.getNodeName();
		System.out.println("nodeName="+nodeName);
		Version version = mainResponse.getVersion();
		System.out.println(version);
		
		
		
		IndexRequest indexRequest = new IndexRequest("megacorp", "employee", "1");
		Map<String, Object> jsonMap = new HashMap<String, Object>();
		jsonMap.put("first_name", "John");
		jsonMap.put("last_name", "Smith");
		jsonMap.put("age", 25);
		jsonMap.put("about", "I love to go rock climbing");
		List<String> interests = new ArrayList<String>();
		interests.add("sports");
		interests.add("music");
		jsonMap.put("interests", interests);
		indexRequest.source(JSON.toJSONString(jsonMap), XContentType.JSON);
		
		
		client.indexAsync(indexRequest, new ActionListener<IndexResponse>() {
			@Override
			public void onResponse(IndexResponse response) {
				String id = response.getId();
				System.out.println("id="+id);
				String index = response.getIndex();
				System.out.println("index="+index);
				long primaryTerm = response.getPrimaryTerm();
				System.out.println("primaryTerm="+primaryTerm);
				DocWriteResponse.Result result = response.getResult();
				System.out.println(result);
				long seqNo = response.getSeqNo();
				System.out.println("seqNo="+seqNo);
				ShardId shardId = response.getShardId();
				System.out.println("shardId="+shardId);
				ShardInfo shardInfo = response.getShardInfo();
				System.out.println("shardInfo="+shardInfo);
				String type = response.getType();
				System.out.println("type="+type);
				long version = response.getVersion();
				System.out.println("version="+version);
			}
			
			@Override
			public void onFailure(Exception e) {
				e.printStackTrace();
			}
		});
		
	}
	
	
	@Test
	public void get() {
		GetRequest request = new GetRequest("megacorp", "employee", "1");
		client.getAsync(request, new ActionListener<GetResponse>() {

			@Override
			public void onResponse(GetResponse getResponse) {
				Map<String, DocumentField> fieldsMap = getResponse.getFields();
				for (Entry<String, DocumentField> entry : fieldsMap.entrySet()) {
					System.out.println(entry.getKey()+" = "+entry.getValue());
				}
				
				String index = getResponse.getIndex();
				System.out.println("index="+index);
				String type = getResponse.getType();
				System.out.println("type="+type);
				String id = getResponse.getId();
				System.out.println("id="+id);
				long version = getResponse.getVersion();
				System.out.println("version="+version);
				boolean found = getResponse.isExists();
				System.out.println("found="+found);
				Map<String, Object> sourceMap = getResponse.getSource();
				for (Entry<String, Object> entry : sourceMap.entrySet()) {
					System.out.println(entry.getKey()+" : "+entry.getValue());
				}
			}

			@Override
			public void onFailure(Exception e) {
				e.printStackTrace();
			}
		});
		while (true) ;
	}
	
	
	@Test
	public void simpleSearch() {
		SearchRequest searchRequest =  new SearchRequest("megacorp");
		searchRequest.types("employee");
		client.searchAsync(searchRequest, new ActionListener<SearchResponse>() {

			@Override
			public void onResponse(SearchResponse response) {
				Aggregations aggregations = response.getAggregations();
				if(aggregations!=null) {
					for (Aggregation aggregation : aggregations) {
						Map<String, Object> metaData = aggregation.getMetaData();
						for (Entry<String, Object> entry : metaData.entrySet()) {
							System.out.println(entry.getKey()+" = "+entry.getValue());
						}
						String aggregationName = aggregation.getName();
						System.out.println("aggregationName="+aggregationName);
						String type = aggregation.getType();
						System.out.println("type="+type);
						System.out.println(aggregation);
					}
				}
				Clusters clusters = response.getClusters();
				int skipped = clusters.getSkipped();
				int successful = clusters.getSuccessful();
				int total = clusters.getTotal();
				System.out.println("skipped="+skipped+" successful="+successful+" total="+total);
				System.out.println(clusters);
				int failedShards = response.getFailedShards();
				System.out.println("failedShards="+failedShards);
				SearchHits searchHits = response.getHits();
				for (SearchHit searchHit : searchHits) {
					int docId = searchHit.docId();
					System.out.println("docId="+docId);
					Map<String, DocumentField> fields = searchHit.getFields();
					for (Map.Entry<String, DocumentField> entry : fields.entrySet()) {
						System.out.println(entry.getKey()+" = "+entry.getValue());
					}
					String clusterAlias = searchHit.getClusterAlias();
					System.out.println("clusterAlias="+clusterAlias);
					Explanation explanation = searchHit.getExplanation();
					if(explanation!=null)
						System.out.println("explanation[+"+explanation.getDescription()+"  "+explanation.getValue()+"]");
					Map<String, HighlightField> highlightFields = searchHit.getHighlightFields();
					for (Map.Entry<String, HighlightField> entry : highlightFields.entrySet()) {
						System.out.println(entry.getKey()+" = "+entry.getValue());
					}
					String id = searchHit.getId();
					System.out.println("id="+id);
					String index = searchHit.getIndex();
					System.out.println("index="+index);
					String type = searchHit.getType();
					System.out.println("type="+type);
					long version = searchHit.getVersion();
					System.out.println("version="+version);
					Map<String, SearchHits> innerSearchHits = searchHit.getInnerHits();
					if(innerSearchHits!=null) 
						for (Map.Entry<String, SearchHits> entry : innerSearchHits.entrySet()) {
							System.out.println(entry.getKey()+" = "+entry.getValue());
						}
					String[] matchedQueries = searchHit.getMatchedQueries();
					for (String matchedQuery : matchedQueries) {
						System.out.println(matchedQuery);
					}
					NestedIdentity nestedIdentity = searchHit.getNestedIdentity();
					System.out.println("NestedIdentity["+nestedIdentity+"]");
					System.out.println(nestedIdentity);
					float score = searchHit.getScore();
					System.out.println("score="+score);
					SearchShardTarget searchShardTarget = searchHit.getShard();
					System.out.println(searchShardTarget);
					Map<String, Object> source = searchHit.getSourceAsMap();
					System.out.println(source);
					
				}
				int numReducePhases = response.getNumReducePhases();
				System.out.println("numReducePhases="+numReducePhases);
				Map<String, ProfileShardResult>  profileShardResults = response.getProfileResults();
				for (Map.Entry<String, ProfileShardResult> entry : profileShardResults.entrySet()) {
					System.out.println(entry.getKey()+" = "+entry.getValue());
				}
				String scrollId = response.getScrollId();
				System.out.println("scrollId="+scrollId);
				ShardSearchFailure[] shardFailures = response.getShardFailures();
				for (ShardSearchFailure shardSearchFailure : shardFailures) {
					System.out.println("shardSearchFailure="+shardSearchFailure);
				}
				int skippedShards = response.getSkippedShards();
				System.out.println("skippedShards="+skippedShards);
				int successfulShards = response.getSuccessfulShards();
				System.out.println("successfulShards="+successfulShards);
				Suggest suggest = response.getSuggest();
				System.out.println("suggest="+suggest);
				TimeValue timeValue = response.getTook();
				System.out.println("timeValue="+timeValue);
				int totalShards = response.getTotalShards();
				System.out.println("totalShards="+totalShards);
				RestStatus restStatus = response.status();
				System.out.println("restStatus="+restStatus);
			}

			@Override
			public void onFailure(Exception e) {
				e.printStackTrace();
			}
		});
		
		while(true);
		
	}
	
	@Test
	public void searchMatchQuery(){
		SearchRequest searchRequest = new SearchRequest();
		searchRequest.indices("megacorp");
		searchRequest.types("employee");
		SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
		searchSourceBuilder.query(QueryBuilders.matchQuery("last_name", "Smith"));
		searchRequest.source(searchSourceBuilder);
		
		
		printResponse(searchRequest);
		
		
		Map<String,Object> jsonMap = new HashMap<String, Object>();
		jsonMap.put("query", "this is a test");
		
		searchRequest.indices("megacorp");
		searchRequest.types("employee");
		searchSourceBuilder = new SearchSourceBuilder();
		searchSourceBuilder.query(QueryBuilders.matchQuery("first_name", "John").prefixLength(1).maxExpansions(3).operator(Operator.AND));
		searchRequest.source(searchSourceBuilder);
		printResponse(searchRequest);
		
		
		
		
		
		
	}
	
	
	
	
	@Test
	public void searchQueryMatchAll(){
		SearchRequest searchRequest = new SearchRequest();
		SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
		searchRequest = new SearchRequest();
		searchRequest.indices("megacorp");
		searchRequest.types("employee");
		searchSourceBuilder = new SearchSourceBuilder();
		searchSourceBuilder.query(QueryBuilders.matchAllQuery());
		searchRequest.source(searchSourceBuilder);
		printResponse(searchRequest);
		
		
		searchRequest = new SearchRequest();
		searchRequest.indices("megacorp");
		searchRequest.types("employee");
		searchSourceBuilder = new SearchSourceBuilder();
		searchSourceBuilder.query(QueryBuilders.matchAllQuery().queryName("first_name"));
		searchRequest.source(searchSourceBuilder);
		printResponse(searchRequest);
		
		
	}

	private void printResponse(SearchRequest searchRequest) {
		try {
			SearchResponse response = client.search(searchRequest);
			Aggregations aggregations = response.getAggregations();
			if(aggregations!=null) {
				for (Aggregation aggregation : aggregations) {
					Map<String, Object> metaData = aggregation.getMetaData();
					for (Entry<String, Object> entry : metaData.entrySet()) {
						System.out.println(entry.getKey()+" = "+entry.getValue());
					}
					String aggregationName = aggregation.getName();
					System.out.println("aggregationName="+aggregationName);
					String type = aggregation.getType();
					System.out.println("type="+type);
					System.out.println(aggregation);
				}
			}
			Clusters clusters = response.getClusters();
			int skipped = clusters.getSkipped();
			int successful = clusters.getSuccessful();
			int total = clusters.getTotal();
			System.out.println("skipped="+skipped+" successful="+successful+" total="+total);
			System.out.println(clusters);
			int failedShards = response.getFailedShards();
			System.out.println("failedShards="+failedShards);
			SearchHits searchHits = response.getHits();
			for (SearchHit searchHit : searchHits) {
				int docId = searchHit.docId();
				System.out.println("docId="+docId);
				Map<String, DocumentField> fields = searchHit.getFields();
				for (Map.Entry<String, DocumentField> entry : fields.entrySet()) {
					System.out.println(entry.getKey()+" = "+entry.getValue());
				}
				String clusterAlias = searchHit.getClusterAlias();
				System.out.println("clusterAlias="+clusterAlias);
				Explanation explanation = searchHit.getExplanation();
				if(explanation!=null)
					System.out.println("explanation[+"+explanation.getDescription()+"  "+explanation.getValue()+"]");
				Map<String, HighlightField> highlightFields = searchHit.getHighlightFields();
				for (Map.Entry<String, HighlightField> entry : highlightFields.entrySet()) {
					System.out.println(entry.getKey()+" = "+entry.getValue());
				}
				String id = searchHit.getId();
				System.out.println("id="+id);
				String index = searchHit.getIndex();
				System.out.println("index="+index);
				String type = searchHit.getType();
				System.out.println("type="+type);
				long version = searchHit.getVersion();
				System.out.println("version="+version);
				Map<String, SearchHits> innerSearchHits = searchHit.getInnerHits();
				if(innerSearchHits!=null) 
					for (Map.Entry<String, SearchHits> entry : innerSearchHits.entrySet()) {
						System.out.println(entry.getKey()+" = "+entry.getValue());
					}
				String[] matchedQueries = searchHit.getMatchedQueries();
				for (String matchedQuery : matchedQueries) {
					System.out.println(matchedQuery);
				}
				NestedIdentity nestedIdentity = searchHit.getNestedIdentity();
				System.out.println("NestedIdentity["+nestedIdentity+"]");
				System.out.println(nestedIdentity);
				float score = searchHit.getScore();
				System.out.println("score="+score);
				SearchShardTarget searchShardTarget = searchHit.getShard();
				System.out.println(searchShardTarget);
				Map<String, Object> source = searchHit.getSourceAsMap();
				System.out.println(source);
				
			}
			int numReducePhases = response.getNumReducePhases();
			System.out.println("numReducePhases="+numReducePhases);
			Map<String, ProfileShardResult>  profileShardResults = response.getProfileResults();
			for (Map.Entry<String, ProfileShardResult> entry : profileShardResults.entrySet()) {
				System.out.println(entry.getKey()+" = "+entry.getValue());
			}
			String scrollId = response.getScrollId();
			System.out.println("scrollId="+scrollId);
			ShardSearchFailure[] shardFailures = response.getShardFailures();
			for (ShardSearchFailure shardSearchFailure : shardFailures) {
				System.out.println("shardSearchFailure="+shardSearchFailure);
			}
			int skippedShards = response.getSkippedShards();
			System.out.println("skippedShards="+skippedShards);
			int successfulShards = response.getSuccessfulShards();
			System.out.println("successfulShards="+successfulShards);
			Suggest suggest = response.getSuggest();
			System.out.println("suggest="+suggest);
			TimeValue timeValue = response.getTook();
			System.out.println("timeValue="+timeValue);
			int totalShards = response.getTotalShards();
			System.out.println("totalShards="+totalShards);
			RestStatus restStatus = response.status();
			System.out.println("restStatus="+restStatus);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
}
