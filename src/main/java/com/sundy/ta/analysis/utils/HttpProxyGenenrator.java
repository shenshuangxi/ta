package com.sundy.ta.analysis.utils;

import java.io.IOException;
import java.net.MalformedURLException;
import java.util.List;
import java.util.Map.Entry;

import org.apache.commons.codec.Charsets;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.util.EntityUtils;
import org.junit.Assert;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.gargoylesoftware.htmlunit.BrowserVersion;
import com.gargoylesoftware.htmlunit.FailingHttpStatusCodeException;
import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.WebConnection;
import com.gargoylesoftware.htmlunit.WebRequest;
import com.gargoylesoftware.htmlunit.WebResponse;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import com.google.common.primitives.Chars;
import com.sundy.ta.datasearch.Spider;
import com.sundy.ta.datasearch.Task;
import com.sundy.ta.datasearch.downloader.Downloader;
import com.sundy.ta.datasearch.downloader.HttpClientRequestContext;
import com.sundy.ta.datasearch.model.Page;
import com.sundy.ta.datasearch.model.Request;
import com.sundy.ta.datasearch.model.ResultItems;
import com.sundy.ta.datasearch.pageprocessor.PageProcessor;
import com.sundy.ta.datasearch.pipeline.Pipeline;
import com.sundy.ta.datasearch.proxy.Proxy;
import com.sundy.ta.datasearch.selector.Selectable;
import com.sundy.ta.datasearch.utils.UrlUtils;

public class HttpProxyGenenrator {

	private static class ProxyHttpPageProcessor implements PageProcessor {
		@Override
		public void process(Page page) {
			Selectable bodySelectable = page.getHtml().css("#primary #content #content_detail #proxies_table tr");
			if(bodySelectable!=null && !bodySelectable.nodes().isEmpty()) {
				List<Selectable> selectables = bodySelectable.nodes();
				for (int i = 1; i < selectables.size(); i++) {
					Selectable selectable = selectables.get(i);
					List<String> cssStrings = selectable.css("td","text").all();
					System.out.println(cssStrings);
					
				}
			}
			System.out.println(bodySelectable);
		}
	}
	
	private static class ProxyHttpPipeline implements Pipeline {
		@Override
		public void process(ResultItems resultItems, Task task) {
			for (Entry<String, Object> resultItem : resultItems.getAll().entrySet()) {
				System.out.println(resultItem.getKey()+"---"+resultItem.getValue());
			}
		}
	} 
	
	
	private static class ProxyHttpDownloader implements Downloader {

		private Logger logger = LoggerFactory.getLogger(getClass());
		
		@SuppressWarnings("deprecation")
		@Override
		public Page download(Request request) {
			 if (request == null || request.getSite() == null) {
		            throw new NullPointerException("task or site can not be null");
		     }
			 Page page = Page.fail();
			 try{
				 	String pageHtml = BrowserUtil.getPageSource(request.getUrl());
			        page.setBytes(pageHtml.getBytes(Charsets.UTF_8));
			        page.setCharset(Charsets.UTF_8.name());
			        page.setRawText(new String(pageHtml.getBytes(Charsets.UTF_8), Charsets.UTF_8.name()));
			        page.setUrl(request.getUrl());
			        page.setRequest(request);
			        page.setStatusCode(200);
			        page.setDownloadSuccess(true);
		            onSuccess(request);
		            logger.info("downloading page success {}", request.getUrl());
		            return page;
		        } catch (IOException e) {
		            logger.warn("download page {} error", request.getUrl(), e);
		            onError(request);
		            return page;
		        }
		}

		@Override
		public void setThread(int threadNum) {
		}

		@Override
		public void onSuccess(Request request) {
			
		}

		@Override
		public void onError(Request request) {
			
		}
		
	}
	
	
	public static void homePage_Firefox() throws Exception {
		
	    try (final WebClient webClient = new WebClient(BrowserVersion.FIREFOX_52)) {
	        final HtmlPage page = webClient.getPage("http://www.site-digger.com/html/articles/20110516/proxieslist.html");
	        String html = page.toString();
	        System.out.println(html);
	        
	    }
	}
	
	public static void main(String[] args) {
		Spider.create(UrlUtils.getDomain("http://www.site-digger.com/html/articles/20110516/proxieslist.html"), 
				UrlUtils.getDomain("http://www.site-digger.com/html/articles/20110516/proxieslist.html")).
		setDownloader(new ProxyHttpDownloader()).
		addPageProcessor(new ProxyHttpPageProcessor()).
		addPipeLine(new ProxyHttpPipeline()).
		setRequest(new Request("http://www.site-digger.com/html/articles/20110516/proxieslist.html", "index")).thread(1).run();
	}
	
}
