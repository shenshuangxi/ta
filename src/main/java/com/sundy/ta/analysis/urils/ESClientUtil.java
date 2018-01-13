package com.sundy.ta.analysis.urils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.http.HttpEntity;
import org.apache.http.HttpHost;
import org.apache.http.ParseException;
import org.apache.http.entity.ContentType;
import org.apache.http.nio.entity.NStringEntity;
import org.apache.http.util.EntityUtils;
import org.elasticsearch.Build;
import org.elasticsearch.ElasticsearchException;
import org.elasticsearch.Version;
import org.elasticsearch.action.ActionListener;
import org.elasticsearch.action.DocWriteRequest;
import org.elasticsearch.action.DocWriteRequest.OpType;
import org.elasticsearch.action.DocWriteResponse;
import org.elasticsearch.action.admin.indices.delete.DeleteIndexRequest;
import org.elasticsearch.action.admin.indices.delete.DeleteIndexResponse;
import org.elasticsearch.action.bulk.BackoffPolicy;
import org.elasticsearch.action.bulk.BulkItemResponse;
import org.elasticsearch.action.bulk.BulkProcessor;
import org.elasticsearch.action.bulk.BulkRequest;
import org.elasticsearch.action.bulk.BulkResponse;
import org.elasticsearch.action.delete.DeleteRequest;
import org.elasticsearch.action.delete.DeleteResponse;
import org.elasticsearch.action.get.GetRequest;
import org.elasticsearch.action.get.GetResponse;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.action.main.MainResponse;
import org.elasticsearch.action.search.ClearScrollRequest;
import org.elasticsearch.action.search.ClearScrollResponse;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.action.search.SearchScrollRequest;
import org.elasticsearch.action.search.ShardSearchFailure;
import org.elasticsearch.action.support.IndicesOptions;
import org.elasticsearch.action.support.WriteRequest;
import org.elasticsearch.action.support.replication.ReplicationResponse;
import org.elasticsearch.action.update.UpdateRequest;
import org.elasticsearch.action.update.UpdateResponse;
import org.elasticsearch.client.Client;
import org.elasticsearch.client.Response;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.cluster.ClusterName;
import org.elasticsearch.common.Strings;
import org.elasticsearch.common.text.Text;
import org.elasticsearch.common.unit.ByteSizeUnit;
import org.elasticsearch.common.unit.ByteSizeValue;
import org.elasticsearch.common.unit.Fuzziness;
import org.elasticsearch.common.unit.TimeValue;
import org.elasticsearch.common.xcontent.XContentBuilder;
import org.elasticsearch.common.xcontent.XContentFactory;
import org.elasticsearch.common.xcontent.XContentType;
import org.elasticsearch.index.VersionType;
import org.elasticsearch.index.query.MatchQueryBuilder;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.rest.RestStatus;
import org.elasticsearch.script.Script;
import org.elasticsearch.script.ScriptType;
import org.elasticsearch.search.Scroll;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.elasticsearch.search.aggregations.Aggregation;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.Aggregations;
import org.elasticsearch.search.aggregations.bucket.range.Range;
import org.elasticsearch.search.aggregations.bucket.terms.Terms;
import org.elasticsearch.search.aggregations.bucket.terms.Terms.Bucket;
import org.elasticsearch.search.aggregations.bucket.terms.TermsAggregationBuilder;
import org.elasticsearch.search.aggregations.metrics.avg.Avg;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.fetch.subphase.FetchSourceContext;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightField;
import org.elasticsearch.search.profile.ProfileResult;
import org.elasticsearch.search.profile.ProfileShardResult;
import org.elasticsearch.search.profile.aggregation.AggregationProfileShardResult;
import org.elasticsearch.search.profile.query.CollectorResult;
import org.elasticsearch.search.profile.query.QueryProfileShardResult;
import org.elasticsearch.search.sort.FieldSortBuilder;
import org.elasticsearch.search.sort.ScoreSortBuilder;
import org.elasticsearch.search.sort.SortOrder;
import org.elasticsearch.search.suggest.Suggest;
import org.elasticsearch.search.suggest.SuggestBuilder;
import org.elasticsearch.search.suggest.SuggestBuilders;
import org.elasticsearch.search.suggest.SuggestionBuilder;
import org.elasticsearch.search.suggest.term.TermSuggestion;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.alibaba.fastjson.JSON;

public class ESClientUtil {

	private final static Logger logger = LoggerFactory.getLogger(ESClientUtil.class);
	
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
		
		
		response = restClient.performRequest("GET", "/_cluster/health?pretty");
		System.out.println(EntityUtils.toString(response.getEntity()));
		EntityUtils.consume(entity);
		
		jsonMap.clear();
		
		try {
			Map<String, Object> settings = new HashMap<String, Object>();
			settings.put("number_of_shards", 3);
			settings.put("number_of_replicas", 1);
			jsonMap.put("settings", settings);
			jsonString = JSON.toJSONString(jsonMap);
			response = restClient.performRequest("PUT", "/blog?pretty", params, entity);
			System.out.println(EntityUtils.toString(response.getEntity()));
			EntityUtils.consume(entity);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
		
		restClient.close();
		
		
		
//		String jsonString = "{" +
//		            "\"user\":\"kimchy\"," +
//		            "\"postDate\":\"2013-01-30\"," +
//		            "\"message\":\"trying out Elasticsearch\"" +
//		        "}";
//		HttpEntity entity = new NStringEntity(jsonString, ContentType.APPLICATION_JSON);
		
	}
	
}
