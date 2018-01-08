package com.sundy.ta.analysis;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sundy.ta.datasearch.Spider;
import com.sundy.ta.datasearch.model.Page;
import com.sundy.ta.datasearch.model.Request;
import com.sundy.ta.datasearch.model.Site;
import com.sundy.ta.datasearch.pageprocessor.PageProcessor;

public class CommonSpider {

	private final static Logger logger = LoggerFactory.getLogger(CommonSpider.class);
	
	private static Map<String,String> rootSites = new HashMap<String, String>();
	
	static {
		logger.info("初始化访问域");
		try {
			Properties prop = new Properties();
			prop.load(CommonSpider.class.getClassLoader().getResourceAsStream("indexsite.properties"));
			for(Entry<Object, Object> entry : prop.entrySet()) {
				rootSites.put(entry.getKey().toString(), entry.getValue().toString());
			}
		} catch (IOException e) {
			e.printStackTrace();
			logger.info("初始化访问域异常");
			System.exit(1);
		}
		logger.info("初始化访问域结束");
	}
	
	private class MyPageProcessor implements PageProcessor {

		private Site site = Site.me().setRetryTimes(3).setSleepTime(100);
		
		@Override
		public void process(Page page) {
			System.out.println(page.getUrl());
//			List<String> urls = page.getHtml().regex("(http(?=|s)://([a-zA-Z0-9-]{1,62}\\.){0,}([a-zA-Z0-9-]{0,}ebay[a-zA-Z0-9-]{0,}\\.)([a-zA-Z0-9-]{1,62}\\.){0,}([a-zA-Z0-9-]{1,62})(/[a-zA-Z0-9-.?]*){0,})").all();
			List<String> urls = page.getHtml().links().all();
			for(String url : urls) {
				if(url.contains(site.getDomain())) {
					page.addTargetRequest(new Request(url));
					System.out.println(url);
				}
			}
		}

		@Override
		public Site getSite() {
			return site;
		}
	}
	
	public void start(){
		for(String url : rootSites.values()){
			Spider.create().setPageProcessor(new MyPageProcessor()).addUrl(url).thread(Runtime.getRuntime().availableProcessors()).run();
		}
	}
	
	public static void main(String[] args) {
		CommonSpider commonSpider = new CommonSpider();
		commonSpider.start();
	}
	
}
