package com.sundy.ta.analysis;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.URLDecoder;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;

import org.apache.commons.text.StringEscapeUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sundy.ta.datasearch.Spider;
import com.sundy.ta.datasearch.model.Page;
import com.sundy.ta.datasearch.model.Request;
import com.sundy.ta.datasearch.model.Site;
import com.sundy.ta.datasearch.pageprocessor.PageProcessor;
import com.sundy.ta.datasearch.selector.Selectable;
import com.sundy.ta.datasearch.utils.UrlUtils;

import org.jsoup.nodes.Element;

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
	
	private Map<String,String> indexMap = new HashMap<String, String>();
	private class IndexPageProcessor implements PageProcessor {
		private Site site = Site.me().setRetryTimes(3).setSleepTime(100);
		@Override
		public void process(Page page) {
			if(page.getUrl().endsWith(page.getRequest().getDomain()) || page.getUrl().endsWith(page.getRequest().getDomain()+"/")) {
				List<Selectable> selectables = page.getHtml().css("a[href]").nodes();
				for (Selectable selectable : selectables) {
					String url = selectable.links().all().get(0);
					String title = selectable.css("a[href]","text").all().get(0);
					if(url.contains(page.getRequest().getDomain()+"/global/")){
						indexMap.put(url, title);
					}
				}
				page.addTargetRequests(Arrays.asList(indexMap.keySet().toArray(new String[]{})));
			}
		}
	}
	
	private Map<String,String> secondMap = new HashMap<String, String>();
	private class SecondPageProcessor implements PageProcessor {
		private Site site = Site.me().setRetryTimes(3).setSleepTime(100);
		@Override
		public void process(Page page) {
			if(indexMap.containsKey(page.getUrl())) {
				List<Selectable> selectables = page.getHtml().css("div[class=nav-module]").css("li[data-node-id=1] > ul > li").nodes();
				for (Selectable selectable : selectables) {
					List<String> links = selectable.css("a[class=see-links]").links().all();
					String url = null;
					String title = null;
					if(links.size()==1) {
						url = selectable.css("a[class=see-links]").links().all().get(0);
						title = selectable.css("a[class=see-links]").css("a[href]","allText").all().get(0);
					} else {
						url = selectable.css("a[href]").links().all().get(0);
						title = selectable.css("a[href]","allText").all().get(0);
					}
					if(!url.replace(page.getUrl(), "").trim().equals("") && !url.replace(page.getUrl(), "").replace("#", "").trim().equals("") && url.replace(page.getUrl(), "").trim().indexOf("?")==-1){
						String key = StringEscapeUtils.unescapeHtml4(url);
						String value = StringEscapeUtils.unescapeHtml4(title);
						secondMap.put(key, value);
					}
				}
				System.out.println(secondMap.size());
				System.out.println(secondMap);
			}
		}
	}
	
	private Map<String,String> thirdMap = new HashMap<String, String>();
	private class ThirdPageProcessor implements PageProcessor {
		private Site site = Site.me().setRetryTimes(3).setSleepTime(100);
		@Override
		public void process(Page page) {
			if(secondMap.containsKey(page.getUrl())) {
				List<Selectable> selectables = page.getHtml().css("div[class=nav-module]").css("li[data-node-id=1] > ul > li").nodes();
				for (Selectable selectable : selectables) {
					List<String> links = selectable.css("a[class=see-links]").links().all();
					String url = null;
					String title = null;
					if(links.size()==1) {
						url = selectable.css("a[class=see-links]").links().all().get(0);
						title = selectable.css("a[class=see-links]").css("a[href]","allText").all().get(0);
					} else {
						url = selectable.css("a[href]").links().all().get(0);
						title = selectable.css("a[href]","allText").all().get(0);
					}
					if(!url.replace(page.getUrl(), "").trim().equals("") && !url.replace(page.getUrl(), "").replace("#", "").trim().equals("") && url.replace(page.getUrl(), "").trim().indexOf("?")==-1){
						String key = StringEscapeUtils.unescapeHtml4(url);
						String value = StringEscapeUtils.unescapeHtml4(title);
						thirdMap.put(key, value);
					}
				}
				System.out.println(thirdMap.size());
				System.out.println(thirdMap);
			}
		}
	}
	
	
	
	
	
	public void start(){
		for(String url : rootSites.values()){
			Spider.create(UrlUtils.getDomain(url), UrlUtils.getDomain(url)).
			addPageProcessor(new IndexPageProcessor()).
			addPageProcessor(new SecondPageProcessor()).
			addUrl(url).thread(1).run();
		}
	}
	
	
	
	public static void main(String[] args) {
		CommonSpider commonSpider = new CommonSpider();
		commonSpider.start();
		
//		Map<String, String> tu = new HashMap<String, String>();
//		
//		BufferedReader br =null;
//		try {
//			br = new BufferedReader(new InputStreamReader(CommonSpider.class.getClassLoader().getResourceAsStream("resourcesebay.html")));
//			StringBuilder sb = new StringBuilder();
//			String line = null;
//			while((line=br.readLine())!=null) {
//				sb.append(line);
//				sb.append("\n");
//			}
//			String text = sb.toString();
//			Page page = new Page();
//			page.setRequest(new Request("https://www.ebay.com/"));
//			page.setRawText(text);
//			Selectable selectable =  page.getHtml().$("div[class=hl-cat-nav__flyout]");
//			Selectable navSelectable = selectable.nodes().get(selectable.nodes().size()-1);
//			List<String> titles = navSelectable.css("a[href]","innerHtml").all();
//			List<String> urls = navSelectable.links().all();
//			for (int i=0;i<titles.size();i++) {
//				tu.put(StringEscapeUtils.unescapeHtml4(titles.get(i)), StringEscapeUtils.unescapeHtml4(urls.get(i)));
//			}
//			System.out.println(tu);
//		} catch (IOException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		} finally {
//			try {
//				if(br!=null)
//				br.close();
//			} catch (IOException e) {
//				// TODO Auto-generated catch block
//				e.printStackTrace();
//			}
//		}
//		BufferedWriter bw = null;
//		try {
//			bw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(new File(page.getRequest().getDomain()+".html"))));
//			bw.write(page.getRawText());
//			bw.close();
//		} catch (IOException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}finally {
//			if(bw!=null)
//				try {
//					bw.close();
//				} catch (IOException e) {
//					// TODO Auto-generated catch block
//					e.printStackTrace();
//				}
//		}
	}
	
}
