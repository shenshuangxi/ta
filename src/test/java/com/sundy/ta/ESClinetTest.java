package com.sundy.ta;

import java.io.IOException;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.http.HttpHost;
import org.elasticsearch.Build;
import org.elasticsearch.ElasticsearchException;
import org.elasticsearch.Version;
import org.elasticsearch.action.ActionListener;
import org.elasticsearch.action.DocWriteRequest;
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
import org.elasticsearch.search.aggregations.bucket.terms.TermsAggregationBuilder;
import org.elasticsearch.search.aggregations.bucket.terms.Terms.Bucket;
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
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.alibaba.fastjson.JSON;
import com.sundy.ta.analysis.urils.ESClientUtil;

public class ESClinetTest {

	private final static Logger logger = LoggerFactory.getLogger(ESClinetTest.class);
	
	public static void main(String[] args) {
		RestHighLevelClient client = null;
		try {
			client = new RestHighLevelClient(RestClient.builder(new HttpHost("192.168.137.4", 10100, "http")));
			
			/**
			 * 删除索引api
			 */
			{
				DeleteIndexRequest deleteIndexRequest = new DeleteIndexRequest("posts");
				//用于等待所有节点知道索引被删除的时间
				deleteIndexRequest.timeout(TimeValue.timeValueMinutes(2));
				//第二种写法      deleteIndexRequest.timeout("2m");
				
				//用于连接主节点耗时
				deleteIndexRequest.masterNodeTimeout(TimeValue.timeValueMinutes(1));
				//用于连接主节点耗时第二种写法   deleteIndexRequest.masterNodeTimeout("1m");
				
				//IndicesOptions 用于控制解决 不可用索引的方式  以及通配符扩展方式
				deleteIndexRequest.indicesOptions(IndicesOptions.lenientExpandOpen());
				
				
				//同步执行
				DeleteIndexResponse deleteIndexResponse = client.indices().deleteIndex(deleteIndexRequest);
				
				//异步执行
				client.indices().deleteIndexAsync(deleteIndexRequest, new ActionListener<DeleteIndexResponse>() {
					
					@Override
					public void onResponse(DeleteIndexResponse deleteIndexResponse) {
						//执行成功调用
					}
					
					@Override
					public void onFailure(Exception e) {
						//执行失败调用
					}
				});
				
				
				//判断 是否所有的节点已都知道该请求
				boolean acknowledged = deleteIndexResponse.isAcknowledged();
				
				
				try {
					DeleteIndexRequest request = new DeleteIndexRequest("does_not_exist");
					client.indices().deleteIndex(request);
				} catch (ElasticsearchException exception) {
					if (exception.status() == RestStatus.NOT_FOUND) {
				        //节点未找到异常
				    }
				}
			}
			
			
			/**
			 * 索引api
			 */
			{
				//索引数据提供方式
				{
					Map<String, Object> jsonMap = new HashMap<String, Object>();
					jsonMap.put("user", "kimchy");
					jsonMap.put("postDate", "2013-01-30");
					jsonMap.put("message", "trying out Elasticsearch");
					
					//第一种方式
					{
						/**
						 * 三个参数 分别是    索引(index)   类型(type)  文档id(document id) 
						 */
						IndexRequest request = new IndexRequest("posts", "doc", "1");
						//文档数据以string类型
						String jsonString = JSON.toJSONString(jsonMap);
						request.source(jsonString, XContentType.JSON);
					}
					
					
					//第二种方式
					{
						IndexRequest request = new IndexRequest("posts", "doc", "1").source(jsonMap);
					}
					
					//第三种方式
					{
						XContentBuilder builder = XContentFactory.jsonBuilder();
						builder.startObject();
						{
							builder.field("user", "kimchy");
						    builder.field("postDate", new Date());
						    builder.field("message", "trying out Elasticsearch");
						}
						builder.endObject();
						
						IndexRequest indexRequest = new IndexRequest("posts", "doc", "1").source(builder);
					}
					
					//第四种方式
					{
						IndexRequest indexRequest = new IndexRequest("posts", "doc", "1").source("user", "kimchy","postDate", new Date(),"message", "trying out Elasticsearch"); 
					}
				}
				
				//可选参数
				{
					IndexRequest request = new IndexRequest("posts", "doc", "1");
					//控制请求分片的路由 使用该值的散列值来寻找分片 而不是id
					request.routing("routing");
					
					//设置该文档的父Id
					request.parent("parent");
					
					//等待分片索引可用的时间
					request.timeout(TimeValue.timeValueSeconds(1));
					
					//刷新策略
					request.setRefreshPolicy(WriteRequest.RefreshPolicy.WAIT_UNTIL);
					
					//设置版本  只有版本匹配才操作 否则忽略
					request.version(2);
					
					//设置操作类型
					request.opType(DocWriteRequest.OpType.CREATE);
					
					//文档获取钱执行的流水线
					request.setPipeline("pipeline");
					
				}
				
				//同步执行
				{
					IndexResponse indexResponse = client.index(new IndexRequest("posts", "doc", "1"));
				}
				
				//异步执行
				{
					client.indexAsync(new IndexRequest("posts", "doc", "1"), new ActionListener<IndexResponse>() {
						
						@Override
						public void onResponse(IndexResponse indexResponse) {
							//执行成功
						}
						
						@Override
						public void onFailure(Exception e) {
							//执行失败
						}
					});
				}
				
				//反馈信息
				{
					IndexResponse indexResponse = client.index(new IndexRequest("posts", "doc", "1"));
					String index = indexResponse.getIndex();
					String type = indexResponse.getType();
					String id = indexResponse.getId();
					long version = indexResponse.getVersion();
					if(DocWriteResponse.Result.CREATED == indexResponse.getResult()) {
						//文档第一创建 后续处理
					}
					if(DocWriteResponse.Result.UPDATED == indexResponse.getResult()) {
						//文档更新
					}
					ReplicationResponse.ShardInfo shardInfo = indexResponse.getShardInfo();
					if(shardInfo.getTotal() != shardInfo.getSuccessful()) {
						//成功分片数 小于总分片数
					}
					if (shardInfo.getFailed() > 0) {
						//失败分片原因 及处理
					    for (ReplicationResponse.ShardInfo.Failure failure : shardInfo.getFailures()) {
					        String reason = failure.reason(); 
					    }
					}
					
					
					IndexRequest request = new IndexRequest("posts", "doc", "1")
			        .source("field", "value")
			        .version(1);
					try {
					    IndexResponse response = client.index(request);
					} catch(ElasticsearchException e) {
					    if (e.status() == RestStatus.CONFLICT) {
					    	//处理版本冲突
					    }
					}
					
					
					request = new IndexRequest("posts", "doc", "1")
			        .source("field", "value")
			        .opType(DocWriteRequest.OpType.CREATE);
					try {
					    IndexResponse response = client.index(request);
					} catch(ElasticsearchException e) {
					    if (e.status() == RestStatus.CONFLICT) {
					        //处理文档已存在的情况
					    }
					}
				}
			}
			
			/**
			 * 查询api
			 */
			{
				//必须参数
				{
					//索引(index) 类型(type)  文档id(document id)
					GetRequest getRequest = new GetRequest("posts", "doc", "1");
				}
				
				//可选参数
				{
					//
					GetRequest request = new GetRequest("posts", "doc", "1");
					//用于控制返回什么样的数据
					request.fetchSourceContext(new FetchSourceContext(false));
					
					String[] includes = new String[]{"message", "*Date"};
					String[] excludes = Strings.EMPTY_ARRAY;
					FetchSourceContext fetchSourceContext = new FetchSourceContext(true, includes, excludes);
					request.fetchSourceContext(fetchSourceContext);
					
					
					request.preference("preference"); 
					request.realtime(false);
					request.refresh(true);
					request.versionType(VersionType.EXTERNAL);
				}
				
				//同步获取
				{
					GetResponse getResponse = client.get(new GetRequest("posts", "doc", "1"));
				}
				
				//异步获取
				{
					client.getAsync(new GetRequest("posts", "doc", "1"), new ActionListener<GetResponse>() {
					    @Override
					    public void onResponse(GetResponse getResponse) {
					        
					    }

					    @Override
					    public void onFailure(Exception e) {
					        
					    }
					});
				}
				
				
				//获取返回信息
				{
					GetRequest request = new GetRequest("posts", "doc", "1");
				    GetResponse getResponse = client.get(request);
					String index = getResponse.getIndex();
					String type = getResponse.getType();
					String id = getResponse.getId();
					if (getResponse.isExists()) {
					    long version = getResponse.getVersion();
					    String sourceAsString = getResponse.getSourceAsString();        
					    Map<String, Object> sourceAsMap = getResponse.getSourceAsMap(); 
					    byte[] sourceAsBytes = getResponse.getSourceAsBytes();          
					} else {
					    
					}
					
					//不存在异常
					request = new GetRequest("does_not_exist", "doc", "1");
					try {
					    getResponse = client.get(request);
					} catch (ElasticsearchException e) {
					    if (e.status() == RestStatus.NOT_FOUND) {
					        
					    }
					}
					
					//版本不一致异常
					try {
					    request = new GetRequest("posts", "doc", "1").version(2);
					    getResponse = client.get(request);
					} catch (ElasticsearchException exception) {
					    if (exception.status() == RestStatus.CONFLICT) {
					        
					    }
					}
				}
			}
			
			
			/**
			 * 删除api
			 */
			{
				//必要参数
				{
					DeleteRequest deleteRequest = new DeleteRequest("posts", "doc", "1");
				}
				
				//可选参数
				{
					DeleteRequest request = new DeleteRequest("posts", "doc", "1");
					request.routing("routing"); 
					request.parent("parent");
					request.timeout(TimeValue.timeValueMinutes(2)); 
					request.timeout("2m");
					request.setRefreshPolicy(WriteRequest.RefreshPolicy.WAIT_UNTIL); 
					request.setRefreshPolicy("wait_for");   
					request.version(2);
					request.versionType(VersionType.EXTERNAL);
				}
				
				//同步执行
				{
					DeleteResponse deleteResponse = client.delete(new DeleteRequest("posts", "doc", "1"));
				}
				
				//同步执行
				{
					client.deleteAsync(new DeleteRequest("posts", "doc", "1"), new ActionListener<DeleteResponse>() {
					    @Override
					    public void onResponse(DeleteResponse deleteResponse) {
					        
					    }

					    @Override
					    public void onFailure(Exception e) {
					        
					    }
					});
				}
				
				//返回信息
				{
					DeleteResponse deleteResponse = client.delete(new DeleteRequest("posts", "doc", "1"));
					
					String index = deleteResponse.getIndex();
					String type = deleteResponse.getType();
					String id = deleteResponse.getId();
					long version = deleteResponse.getVersion();
					ReplicationResponse.ShardInfo shardInfo = deleteResponse.getShardInfo();
					if (shardInfo.getTotal() != shardInfo.getSuccessful()) {
					    
					}
					if (shardInfo.getFailed() > 0) {
					    for (ReplicationResponse.ShardInfo.Failure failure : shardInfo.getFailures()) {
					        String reason = failure.reason(); 
					    }
					}
					
					//未找到文档
					DeleteRequest request = new DeleteRequest("posts", "doc", "does_not_exist");
					deleteResponse = client.delete(request);
					if (deleteResponse.getResult() == DocWriteResponse.Result.NOT_FOUND) {
					    
					}
					
					//版本不匹配
					try {
					    request = new DeleteRequest("posts", "doc", "1").version(2);
					    deleteResponse = client.delete(request);
					} catch (ElasticsearchException exception) {
					    if (exception.status() == RestStatus.CONFLICT) {
					        
					    }
					}
					
				}
				
				
			}
			
			
			/**
			 * 更新api
			 */
			{
				//必须参数
				{
					// 索引 (index)  类型(type)  文档id(document id)
					UpdateRequest request = new UpdateRequest("posts", "doc", "1");
				}
				
				//带脚本更新
				{
					UpdateRequest request = new UpdateRequest("posts", "doc", "1");
					
					Map<String, Object> params = new HashMap<String, Object>();
					params.put("count", 4);
					
					
					//脚本语言 Painless, Mustache, and Expressions
					Script inline = new Script(ScriptType.INLINE, "painless", "ctx._source.field += params.count", params);
					
					//执行脚本  需要注意每次执行最好使用相同的脚本(脚本需要预编译) 但可以改变执行参数
					request.script(inline);
					
					
					Script stored = new Script(ScriptType.STORED, null, "increment-field", params);  
					request.script(stored); 
					
				}
				
				//部分更新
				{
					UpdateRequest request = new UpdateRequest("posts", "doc", "1");
					Map<String, Object> jsonMap = new HashMap<String, Object>();
					jsonMap.put("updated", "2017-01-01");
					jsonMap.put("reason", "daily update");
					request.doc(JSON.toJSONString(jsonMap), XContentType.JSON);
				}
				
				//更新  如果没有就新建
				{
					UpdateRequest request = new UpdateRequest("posts", "doc", "1");
					String jsonString = "{\"created\":\"2017-01-01\"}";
					request.upsert(jsonString, XContentType.JSON);
				}
				
				
			}
			
			/**
			 * 批量请求 API
			 */
			{
				//必须参数
				BulkRequest request = new BulkRequest();
				request.add(new IndexRequest("posts","doc","1").source(XContentType.JSON,"field","foo"))
				.add(new IndexRequest("posts","doc","2").source(XContentType.JSON, "field","bar"))
				.add(new IndexRequest("posts","doc","3").source(XContentType.JSON, "field","baz"));
				
				//可选参数
				request.timeout(TimeValue.timeValueMinutes(2));
				request.setRefreshPolicy(WriteRequest.RefreshPolicy.WAIT_UNTIL);
				request.waitForActiveShards(2);
				
				//同步处理
				{
					BulkResponse response = client.bulk(request);
				}
				
				//异步处理
				{
					client.bulkAsync(request, new ActionListener<BulkResponse>() {
						
						@Override
						public void onResponse(BulkResponse response) {
							// TODO Auto-generated method stub
							
						}
						
						@Override
						public void onFailure(Exception e) {
							// TODO Auto-generated method stub
							
						}
					});
				}
				
				//反馈信息
				{
					BulkResponse response = client.bulk(request);
					for (BulkItemResponse bulkItemResponse : response) { 
					    DocWriteResponse itemResponse = bulkItemResponse.getResponse(); 

					    if (bulkItemResponse.getOpType() == DocWriteRequest.OpType.INDEX
					            || bulkItemResponse.getOpType() == DocWriteRequest.OpType.CREATE) { 
					        IndexResponse indexResponse = (IndexResponse) itemResponse;

					    } else if (bulkItemResponse.getOpType() == DocWriteRequest.OpType.UPDATE) { 
					        UpdateResponse updateResponse = (UpdateResponse) itemResponse;

					    } else if (bulkItemResponse.getOpType() == DocWriteRequest.OpType.DELETE) { 
					        DeleteResponse deleteResponse = (DeleteResponse) itemResponse;
					    }
					}
					
					//异常处理
					for (BulkItemResponse bulkItemResponse : response) {
					    if (bulkItemResponse.isFailed()) { 
					        BulkItemResponse.Failure failure = bulkItemResponse.getFailure(); 

					    }
					}
					
				}
				
				//块处理
				{
					BulkProcessor.Listener listener = new BulkProcessor.Listener() {
						
						@Override
						public void beforeBulk(long executionId, BulkRequest request) {
							int numberOfActions = request.numberOfActions(); 
					        logger.debug("Executing bulk [{}] with {} requests", executionId, numberOfActions);
						}
						
						@Override
						public void afterBulk(long executionId, BulkRequest request,
								Throwable failure) {
							logger.error("Failed to execute bulk", failure);
						}
						
						@Override
						public void afterBulk(long executionId, BulkRequest request,
								BulkResponse response) {
							if (response.hasFailures()) { 
					            logger.warn("Bulk [{}] executed with failures", executionId);
					        } else {
					            logger.debug("Bulk [{}] completed in {} milliseconds", executionId, response.getTook().getMillis());
					        }
						}
					};
					
					BulkProcessor bulkProcessor = BulkProcessor.builder(client::bulkAsync, listener).build();
					
					BulkProcessor.Builder builder = BulkProcessor.builder(client::bulkAsync, listener);
					builder.setBulkActions(500); 
					builder.setBulkSize(new ByteSizeValue(1L, ByteSizeUnit.MB)); 
					builder.setConcurrentRequests(0); 
					builder.setFlushInterval(TimeValue.timeValueSeconds(10L)); 
					builder.setBackoffPolicy(BackoffPolicy.constantBackoff(TimeValue.timeValueSeconds(1L), 3)); 
					
					
					
					IndexRequest one = new IndexRequest("posts", "doc", "1").source(XContentType.JSON, "title", "In which order are my Elasticsearch queries executed?");
					IndexRequest two = new IndexRequest("posts", "doc", "2").source(XContentType.JSON, "title", "Current status and upcoming changes in Elasticsearch");
					IndexRequest three = new IndexRequest("posts", "doc", "3").source(XContentType.JSON, "title", "The Future of Federated Search in Elasticsearch");

					bulkProcessor.add(one);
					bulkProcessor.add(two);
					bulkProcessor.add(three);
					
					
					bulkProcessor.close();

				}
			}
			
			/**
			 * 搜索api
			 */
			{
				//一般基本格式 全匹配
				{
					SearchRequest searchRequest = new SearchRequest();
					SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
					searchSourceBuilder.query(QueryBuilders.matchAllQuery());
				}
				
				//可选参数
				{
					
					SearchRequest searchRequest = new SearchRequest();
					searchRequest.indices("posts");//限制索引 index
					searchRequest.types("doc");//限制类型 type
					
					
					searchRequest.routing("routing"); //路由分片设置
					//设置当索引不可用 如何处理 以及通配符扩展
					searchRequest.indicesOptions(IndicesOptions.lenientExpandOpen());
					
					//设置分片首选项，默认随机访问分片  local  优先本地   _primary 主分片  
					searchRequest.preference("_local");
					
				}
				
				//搜索体构建 SearchSourceBuilder
				{
					SearchSourceBuilder builder = new SearchSourceBuilder();
					builder.query(QueryBuilders.termQuery("user", "kimchy"));
					builder.from(0);
					builder.size(5);
					builder.timeout(TimeValue.timeValueSeconds(60));
					
					SearchRequest searchRequest = new SearchRequest();
					searchRequest.source(builder);
					
					
					//构建查询语句
					MatchQueryBuilder matchQueryBuilder = new MatchQueryBuilder("user", "kimchy");
					matchQueryBuilder.fuzziness(Fuzziness.AUTO);
					matchQueryBuilder.prefixLength(3);
					matchQueryBuilder.maxExpansions(10);
					builder.query(matchQueryBuilder);
					
					//排序
					builder.sort(new ScoreSortBuilder().order(SortOrder.DESC));
					builder.sort(new FieldSortBuilder("_uid").order(SortOrder.DESC));
					
					//过滤  	builder.fetchSource(false);//禁用过滤
					String[] includeFields = new String[]{"title", "user", "innerObject.*"};
					String[] excludeFields = new String[] {"_type"};
					builder.fetchSource(includeFields, excludeFields);
					
					//请求高亮
					HighlightBuilder highlightBuilder = new HighlightBuilder();
					HighlightBuilder.Field highlightTitle = new HighlightBuilder.Field("title");
					highlightTitle.highlighterType("unified");
					highlightBuilder.field(highlightTitle);  
					HighlightBuilder.Field highlightUser = new HighlightBuilder.Field("user");
					highlightBuilder.field(highlightUser);
					builder.highlighter(highlightBuilder);
					
					
					//请求聚合
					TermsAggregationBuilder aggregation = AggregationBuilders.terms("by_company").field("company.keyword");
					aggregation.subAggregation(AggregationBuilders.avg("average_age").field("age"));
					builder.aggregation(aggregation);
					
					//请求建议
					SuggestionBuilder termSuggestionBuilder = SuggestBuilders.termSuggestion("user").text("kmichy"); 
					SuggestBuilder suggestBuilder = new SuggestBuilder();
					suggestBuilder.addSuggestion("suggest_user", termSuggestionBuilder); 
					builder.suggest(suggestBuilder);
					
					builder.profile(true); //分析查询语句
				}
				
				//同步执行
				{
					SearchResponse searchResponse = client.search(new SearchRequest());
				}
				
				//异步执行
				{
					client.searchAsync(new SearchRequest(), new ActionListener<SearchResponse>() {
					    @Override
					    public void onResponse(SearchResponse searchResponse) {
					        
					    }

					    @Override
					    public void onFailure(Exception e) {
					        
					    }
					});
				}
				
				//反馈信息
				{
					SearchResponse searchResponse = client.search(new SearchRequest());
					RestStatus status = searchResponse.status();
					TimeValue took = searchResponse.getTook();
					Boolean terminatedEarly = searchResponse.isTerminatedEarly();
					boolean timedOut = searchResponse.isTimedOut();
					
					int totalShards = searchResponse.getTotalShards();
					int successfulShards = searchResponse.getSuccessfulShards();
					int failedShards = searchResponse.getFailedShards();
					for (ShardSearchFailure failure : searchResponse.getShardFailures()) {
					    // failures should be handled here
					}
					
					
					//获取查询文档
					SearchHits hits = searchResponse.getHits();
					long totalHits = hits.getTotalHits();
					float maxScore = hits.getMaxScore();
					for (SearchHit hit : hits.getHits()) {
						// do something with the SearchHit
						String index = hit.getIndex();
						String type = hit.getType();
						String id = hit.getId();
						float score = hit.getScore();
						String sourceAsString = hit.getSourceAsString();
						Map<String, Object> sourceAsMap = hit.getSourceAsMap();
						String documentTitle = (String) sourceAsMap.get("title");
						List<Object> users = (List<Object>) sourceAsMap.get("user");
						Map<String, Object> innerObject = (Map<String, Object>) sourceAsMap.get("innerObject");
						
						Map<String, HighlightField> highlightFields = hit.getHighlightFields();
					    HighlightField highlight = highlightFields.get("title"); 
					    Text[] fragments = highlight.fragments();  
					    String fragmentString = fragments[0].string();
					}
					
					
					//获取聚合
					Aggregations aggregations = searchResponse.getAggregations();
					Terms byCompanyAggregation = aggregations.get("by_company"); 
					Bucket elasticBucket = byCompanyAggregation.getBucketByKey("Elastic"); 
					Avg averageAge = elasticBucket.getAggregations().get("average_age"); 
					double avg = averageAge.getValue();
					
					Range range = aggregations.get("by_company");
					
					Map<String, Aggregation> aggregationMap = aggregations.getAsMap();
					Terms companyAggregation = (Terms) aggregationMap.get("by_company");
					
					List<Aggregation> aggregationList = aggregations.asList();
					for (Aggregation agg : aggregations) {
					    String type = agg.getType();
					    if (type.equals(TermsAggregationBuilder.NAME)) {
					        Bucket elasticBucket1 = ((Terms) agg).getBucketByKey("Elastic");
					        long numberOfDocs = elasticBucket1.getDocCount();
					    }
					}
					
					
					//获取建议 Suggestion
					Suggest suggest = searchResponse.getSuggest(); 
					TermSuggestion termSuggestion = suggest.getSuggestion("suggest_user"); 
					for (TermSuggestion.Entry entry : termSuggestion.getEntries()) { 
					    for (TermSuggestion.Entry.Option option : entry) { 
					        String suggestText = option.getText().string();
					    }
					}
					
					
					//获取剖析结果
					Map<String, ProfileShardResult> profilingResults = searchResponse.getProfileResults(); 
					for (Map.Entry<String, ProfileShardResult> profilingResult : profilingResults.entrySet()) {  
					    String key = profilingResult.getKey(); 
					    ProfileShardResult profileShardResult = profilingResult.getValue(); 
					    List<QueryProfileShardResult> queryProfileShardResults = profileShardResult.getQueryProfileResults(); 
					    for (QueryProfileShardResult queryProfileResult : queryProfileShardResults) { 
					    	for (ProfileResult profileResult : queryProfileResult.getQueryResults()) { 
						        String queryName = profileResult.getQueryName(); 
						        long queryTimeInMillis = profileResult.getTime(); 
						        List<ProfileResult> profiledChildren = profileResult.getProfiledChildren(); 
						        
						        
						        CollectorResult collectorResult = queryProfileResult.getCollectorResult();  
						        String collectorName = collectorResult.getName();  
						        Long collectorTimeInMillis = collectorResult.getTime(); 
						        List<CollectorResult> profiledChildren1 = collectorResult.getProfiledChildren();
						    }
					    }
					    
					    AggregationProfileShardResult aggsProfileResults = profileShardResult.getAggregationProfileResults(); 
					    for (ProfileResult profileResult : aggsProfileResults.getProfileResults()) { 
					        String aggName = profileResult.getQueryName(); 
					        long aggTimeInMillis = profileResult.getTime(); 
					        List<ProfileResult> profiledChildren = profileResult.getProfiledChildren(); 
					    }
					}
				}
			}
			
			
			/**
			 * 游标查找
			 */
			{
				//基本操作
				{
					SearchRequest searchRequest = new SearchRequest("posts");
					SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
					searchSourceBuilder.query(QueryBuilders.matchQuery("title", "Elasticsearch"));
					searchSourceBuilder.size(10); 
					searchRequest.source(searchSourceBuilder);
					searchRequest.scroll(TimeValue.timeValueMinutes(1L)); 
					SearchResponse searchResponse = client.search(searchRequest);
					String scrollId = searchResponse.getScrollId(); 
					SearchHits hits = searchResponse.getHits(); 
					
					//操作返回数据
					SearchScrollRequest scrollRequest = new SearchScrollRequest(scrollId); 
					scrollRequest.scroll(TimeValue.timeValueSeconds(30));
					SearchResponse searchScrollResponse = client.searchScroll(scrollRequest);
					scrollId = searchScrollResponse.getScrollId();  
					hits = searchScrollResponse.getHits(); 
					long totalHits = hits.getTotalHits();
					
					//可选参数
					scrollRequest.scroll(TimeValue.timeValueSeconds(60L)); 
					scrollRequest.scroll("60s"); 
				}
				
				
				//同步操作
				{
					SearchResponse searchResponse = client.searchScroll(new SearchScrollRequest());
				}
				
				//异步操作
				{
					client.searchScrollAsync(new SearchScrollRequest(), new ActionListener<SearchResponse>() {
					    @Override
					    public void onResponse(SearchResponse searchResponse) {
					        
					    }

					    @Override
					    public void onFailure(Exception e) {
					        
					    }
					});
				}
				
				//完整示例
				{
					final Scroll scroll = new Scroll(TimeValue.timeValueMinutes(1L));
					SearchRequest searchRequest = new SearchRequest("posts");
					searchRequest.scroll(scroll);
					SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
					searchSourceBuilder.query(QueryBuilders.matchQuery("title", "Elasticsearch"));
					searchRequest.source(searchSourceBuilder);

					SearchResponse searchResponse = client.search(searchRequest); 
					String scrollId = searchResponse.getScrollId();
					SearchHit[] searchHits = searchResponse.getHits().getHits();

					while (searchHits != null && searchHits.length > 0) { 
					    SearchScrollRequest scrollRequest = new SearchScrollRequest(scrollId); 
					    scrollRequest.scroll(scroll);
					    searchResponse = client.searchScroll(scrollRequest);
					    scrollId = searchResponse.getScrollId();
					    searchHits = searchResponse.getHits().getHits();
					    
					}

					ClearScrollRequest clearScrollRequest = new ClearScrollRequest(); 
					clearScrollRequest.addScrollId(scrollId);
					ClearScrollResponse clearScrollResponse = client.clearScroll(clearScrollRequest);
					boolean succeeded = clearScrollResponse.isSucceeded();
				}
				
				
			}
			
			
			/**
			 * info api
			 */
			{
				//基本写法
				MainResponse response = client.info();
				
				ClusterName clusterName = response.getClusterName();
				String clusterUuid = response.getClusterUuid();
				String nodeName = response.getNodeName();
				Version version = response.getVersion();
				Build build = response.getBuild();
				
				
			}
			
		} catch (Exception e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}finally {
			if(client!=null) {
				try {
					client.close();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
	}
	
	
	
}
