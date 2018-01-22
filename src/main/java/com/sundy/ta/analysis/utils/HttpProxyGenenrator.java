package com.sundy.ta.analysis.utils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.locks.ReentrantLock;

import org.apache.commons.codec.Charsets;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.gargoylesoftware.htmlunit.BrowserVersion;
import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import com.sundy.ta.datasearch.Spider;
import com.sundy.ta.datasearch.constant.Constant;
import com.sundy.ta.datasearch.downloader.Downloader;
import com.sundy.ta.datasearch.downloader.HttpClientGenerator;
import com.sundy.ta.datasearch.downloader.HttpClientRequestContext;
import com.sundy.ta.datasearch.downloader.HttpUriRequestConverter;
import com.sundy.ta.datasearch.model.Page;
import com.sundy.ta.datasearch.model.Request;
import com.sundy.ta.datasearch.pageprocessor.PageProcessor;
import com.sundy.ta.datasearch.proxy.Proxy;
import com.sundy.ta.datasearch.selector.Selectable;
import com.sundy.ta.datasearch.utils.UrlUtils;

import lombok.Getter;

public class HttpProxyGenenrator {

	private static List<HttpProxy> transparentHttpProxys = new ArrayList<>(); 
	private static List<HttpProxy> anonymousHttpProxys = new ArrayList<>(); 
	private final static AtomicLong pointer = new AtomicLong();
	private static Spider spider;
	private final static ReentrantLock lock = new ReentrantLock(); 
	
	private final static HttpProxyGenenrator genenrator = new HttpProxyGenenrator(); 
	
	private final static HttpClientGenerator httpClientGenerator = HttpClientGenerator.build();
	
	public static HttpProxyGenenrator getGenenrator() {
		return genenrator;
	}
	
	public Proxy getProxy(Request request) {
		lock.lock();
		try {
			HttpProxy httpProxy = anonymousHttpProxys.get(incrForLoop());
			return new Proxy(httpProxy.getHost(), httpProxy.getPort());
		} finally {
			lock.unlock();
		}
	}

	static {
		ScheduledExecutorService scheduledExecutorService = Executors.newSingleThreadScheduledExecutor();
		scheduledExecutorService.scheduleWithFixedDelay(new Runnable() {
			public void run() {
				lock.lock();
				try {
					spider = Spider.create(UrlUtils.getDomain("http://www.site-digger.com/html/articles/20110516/proxieslist.html"), 
							UrlUtils.getDomain("http://www.site-digger.com/html/articles/20110516/proxieslist.html")).
					setDownloader(new ProxyHttpDownloader()).
					addPageProcessor(new ProxyHttpPageProcessor()).
					setRequest(new Request("http://www.site-digger.com/html/articles/20110516/proxieslist.html", "index")).thread(1);
					spider.run();
				} catch (Throwable e) {
					e.printStackTrace();
				} finally {
					lock.unlock();
				}
			}
		}, 0, 2, TimeUnit.HOURS);
	}
	
	private static int incrForLoop() {
        long p = pointer.incrementAndGet();
        int size = anonymousHttpProxys.size();
        if (p < size) {
            return (int) p;
        }
        while (!pointer.compareAndSet(p, p % size)) {
            p = pointer.get();
        }
        return (int) (p % size);
    }
	
	@Getter
	public static class HttpProxy {
		private final String host;
		private final int port;
		private final String address;
		public HttpProxy(String host, int port, String address) {
			this.host = host;
			this.port = port;
			this.address = address;
		}
	}
	
	private static class ProxyHttpPageProcessor implements PageProcessor {
		@Override
		public void process(Page page) {
			try {
				Selectable bodySelectable = page.getHtml().css("#primary #content #content_detail #proxies_table tr");
				if(bodySelectable!=null && !bodySelectable.nodes().isEmpty()) {
					List<Selectable> selectables = bodySelectable.nodes();
					List<HttpProxy> transparentHttpProxys = new ArrayList<>(); 
					List<HttpProxy> anonymousHttpProxys = new ArrayList<>(); 
					for (int i = 1; i < selectables.size(); i++) {
						Selectable selectable = selectables.get(i);
						List<String> cssStrings = selectable.css("td","text").all();
						String[] ipPort = cssStrings.get(0).split(":");
						String host = ipPort[0];
						int port = Integer.valueOf(ipPort[1]);
						String address = cssStrings.get(2);
						HttpProxy httpProxy = new HttpProxy(host, port, address);
						if(cssStrings.get(1).equals("Transparent")) {
							transparentHttpProxys.add(httpProxy);
						} else {
							CloseableHttpResponse httpResponse = null;
							try {
								Request request = new Request("https://pages.ebay.com/sitemap.html", "test");
								HttpUriRequestConverter httpUriRequestConverter = new HttpUriRequestConverter();
								CloseableHttpClient httpClient = httpClientGenerator.getClient(request.getSite());
								Proxy proxy = new Proxy(host, port);
								HttpClientRequestContext requestContext = httpUriRequestConverter.convert(request, request.getSite(), proxy);
								httpResponse = httpClient.execute(requestContext.getHttpUriRequest(), requestContext.getHttpClientContext());
								if(httpResponse.getStatusLine().getStatusCode()==Constant.StatusCode.OK.getCode()) {
									anonymousHttpProxys.add(httpProxy);
								}
							} catch (Exception e) {
								e.printStackTrace();
							}finally {
								if (httpResponse != null) {
					                EntityUtils.consumeQuietly(httpResponse.getEntity());
					            }
							}
							
						}
					}
					HttpProxyGenenrator.transparentHttpProxys.clear();
					HttpProxyGenenrator.anonymousHttpProxys.clear();
					HttpProxyGenenrator.transparentHttpProxys.addAll(transparentHttpProxys);
					HttpProxyGenenrator.anonymousHttpProxys.addAll(anonymousHttpProxys);
				}
			} finally {
				spider.close();
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
	
}
