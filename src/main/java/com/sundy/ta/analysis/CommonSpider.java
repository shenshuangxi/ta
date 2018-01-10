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
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;

import org.apache.commons.text.StringEscapeUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sundy.ta.datasearch.Spider;
import com.sundy.ta.datasearch.Task;
import com.sundy.ta.datasearch.model.Page;
import com.sundy.ta.datasearch.model.Request;
import com.sundy.ta.datasearch.model.ResultItems;
import com.sundy.ta.datasearch.model.Site;
import com.sundy.ta.datasearch.pageprocessor.PageProcessor;
import com.sundy.ta.datasearch.pipeline.Pipeline;
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
	
	
	
	
	
	
	private class FirstPageProcessor implements PageProcessor {
		@Override
		public void process(Page page) {
			if(page.getRequest().getNavgator().equals("index")) {
				List<Selectable> selectables = page.getHtml().css("div#buy > div[class=h3content]").nodes().get(0).css("a[href]").nodes();
				for (Selectable selectable : selectables) {
					String url = selectable.css("a[href]").links().all().get(0);
					String title = selectable.css("a[href]","allText").all().get(0);
					if(!url.replace(page.getUrl(), "").trim().equals("") && !url.replace(page.getUrl(), "").replace("#", "").trim().equals("") && url.replace(page.getUrl(), "").trim().indexOf("?")==-1){
						String key = StringEscapeUtils.unescapeHtml4(url);
						String value = StringEscapeUtils.unescapeHtml4(title);
						page.addTargetRequest(new Request(key, value, Site.me().setRetryTimes(3).setSleepTime(100)));
					}
				}
			}
		}
	}
	
	private class FirstSecondPageProcessor implements PageProcessor {
		@Override
		public void process(Page page) {
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
					page.addTargetRequest(new Request(key, value, Site.me().setRetryTimes(3).setSleepTime(100)));
				}
			}
		}
	}
	
	private class SecondPageProcessor implements PageProcessor {
		@Override
		public void process(Page page) {
			Selectable selectable = page.getHtml().css("div#CenterPanelInner");
			String listCounts = selectable.css("#brcnt span.listingscnt","allText").get();
			if(listCounts!=null && !listCounts.trim().equals("")) {
				System.out.println(listCounts);
				List<Selectable> viewSelectables = selectable.css("#ResultSetItems > #ListViewInner > li[class=sresult lvresult clearfix li]").nodes();
				for (Selectable viewSelectable : viewSelectables) {
					Selectable imgLinks = viewSelectable.css("div[class=lvpic pic img left] > div[class=lvpicinner full-width picW] > a[href]");
					System.out.println(imgLinks);
					try {
						String url = imgLinks.css("a[href]").links().all().get(0);
						String title = imgLinks.css("a[href] > img","alt").all().get(0);
						String key = StringEscapeUtils.unescapeHtml4(url);
						String value = StringEscapeUtils.unescapeHtml4(title);
						page.addTargetRequest(new Request(key, value, Site.me().setRetryTimes(3).setSleepTime(100)));
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
				//翻页
				Selectable nextPageSelectable = selectable.css("#PaginationAndExpansionsContainer #pgCtrlTbl #Pagination  td[class=pagn-next]  a[class=gspr next]");
				String key = StringEscapeUtils.unescapeHtml4(nextPageSelectable.links().get());
				String value = page.getRequest().getNavgator();
				page.addTargetRequest(new Request(key, value, Site.me().setRetryTimes(3).setSleepTime(100)));
			}
		}
	}
	
	
	private class ThirdPageProcessor implements PageProcessor {
		@Override
		public void process(Page page) {
			Selectable bodySelectable = page.getHtml().css("#Body");
			String categoryDetails = bodySelectable.css("div#TopPanel td#vi-VR-brumb-lnkLst","allText").get();
			if(categoryDetails!=null && !categoryDetails.trim().equals("")) {
				Selectable centryBodySelectable = bodySelectable.css("div#CenterPanelInternal");
				String pictureLink = centryBodySelectable.css("#PicturePanel img#icImg","src").get();
				if(pictureLink!=null && !pictureLink.trim().equals("")) { 
					String title = centryBodySelectable.css("#itemTitle", "text").get();
					if(title!=null && !title.trim().equals("")) { 
						String notification = centryBodySelectable.css("#vi_notification_new","allText").get();
						Selectable rightSelectable = centryBodySelectable.css("div#RightSummaryPanel");
						String storekeeper = rightSelectable.css("#mbgLink","allText").get();
						String feedbackScore = rightSelectable.css("span[class=mbg-l] a[href]","text").get();
						String feedbackScorePercent = rightSelectable.css("div#si-fb","text").get();
						Selectable leftSelectable = centryBodySelectable.css("div#LeftSummaryPanel  div#mainContent");
						String bids = leftSelectable.css("#vi-VR-bid-lnk","allText").get();
						String price = leftSelectable.css("#prcIsum","text").get();
						if(price==null||price.trim().equals("")) {
							price = leftSelectable.css("#mm-saleDscPrc","text").get();
						}
						String watchs = leftSelectable.css("#vi-bybox-watchers","allText").get();
						String whyToBuy = leftSelectable.css("#why2buy", "allText").get();
						String shipping = leftSelectable.css("#shSummary #fshippingCost","allText").get();
						page.putField("pictureLink", pictureLink);
						page.putField("categoryDetails", categoryDetails);
						page.putField("title", title);
						page.putField("notification", notification);
						page.putField("storekeeper", storekeeper);
						page.putField("feedbackScore", feedbackScore);
						page.putField("feedbackScorePercent", feedbackScorePercent);
						page.putField("bids", bids);
						page.putField("price", price);
						page.putField("watchs", watchs);
						page.putField("whyToBuy", whyToBuy);
						page.putField("shipping", shipping);
						page.putField("url", page.getUrl());
					}
				}
			}
			
		}
	}
	
	private class ESPipeline implements Pipeline {
		@Override
		public void process(ResultItems resultItems, Task task) {
			
			
		}
	} 
	
	
	
	
	
	public void start(){
		for(String url : rootSites.values()){
			Spider.create(UrlUtils.getDomain(url), UrlUtils.getDomain(url)).
			addPageProcessor(new FirstPageProcessor()).
			addPageProcessor(new FirstSecondPageProcessor()).
			addPageProcessor(new SecondPageProcessor()).
			addPageProcessor(new ThirdPageProcessor()).
			setRequest(new Request(url, "index")).thread(1).run();
		}
	}
	
	
	
	public static void main(String[] args) {
//		CommonSpider commonSpider = new CommonSpider();
//		commonSpider.start();
		
		BufferedReader br =null;
		try {
			br = new BufferedReader(new InputStreamReader(CommonSpider.class.getClassLoader().getResourceAsStream("details.html")));
			StringBuilder sb = new StringBuilder();
			String line = null;
			while((line=br.readLine())!=null) {
				sb.append(line);
				sb.append("\n");
			}
			String text = sb.toString();
			Page page = new Page();
			page.setRequest(new Request("https://www.ebay.com/","asdf"));
			page.setRawText(text);
			Selectable bodySelectable = page.getHtml().css("#Body");
			String categoryDetails = bodySelectable.css("div#TopPanel td#vi-VR-brumb-lnkLst","allText").get();
			if(categoryDetails!=null && !categoryDetails.trim().equals("")) {
				Selectable centryBodySelectable = bodySelectable.css("div#CenterPanelInternal");
				String pictureLink = centryBodySelectable.css("#PicturePanel img#icImg","src").get();
				if(pictureLink!=null && !pictureLink.trim().equals("")) { 
					String title = centryBodySelectable.css("#itemTitle", "text").get();
					if(title!=null && !title.trim().equals("")) { 
						String notification = centryBodySelectable.css("#vi_notification_new","allText").get();
						Selectable rightSelectable = centryBodySelectable.css("div#RightSummaryPanel");
						String storekeeper = rightSelectable.css("#mbgLink","allText").get();
						String feedbackScore = rightSelectable.css("span[class=mbg-l] a[href]","text").get();
						String feedbackScorePercent = rightSelectable.css("div#si-fb","text").get();
						Selectable leftSelectable = centryBodySelectable.css("div#LeftSummaryPanel  div#mainContent");
						String bids = leftSelectable.css("#vi-VR-bid-lnk","allText").get();
						String price = leftSelectable.css("#prcIsum","text").get();
						if(price==null||price.trim().equals("")) {
							price = leftSelectable.css("#mm-saleDscPrc","text").get();
						}
						String watchs = leftSelectable.css("#vi-bybox-watchers","allText").get();
						String whyToBuy = leftSelectable.css("#why2buy", "allText").get();
						String shipping = leftSelectable.css("#shSummary #fshippingCost","allText").get();
						System.out.println(categoryDetails);
						System.out.println(pictureLink);
						System.out.println(title);
						System.out.println(notification);
						System.out.println(storekeeper);
						System.out.println(feedbackScore);
						System.out.println(feedbackScorePercent);
						System.out.println(bids);
						System.out.println(price);
						System.out.println(watchs);
						System.out.println(whyToBuy);
						System.out.println(shipping);
						page.putField("categoryDetails", categoryDetails);
						page.putField("pictureLink", pictureLink);
						page.putField("title", title);
						page.putField("notification", notification);
						page.putField("storekeeper", storekeeper);
						page.putField("feedbackScore", feedbackScore);
						page.putField("feedbackScorePercent", feedbackScorePercent);
						page.putField("bids", bids);
						page.putField("price", price);
						page.putField("watchs", watchs);
						page.putField("whyToBuy", whyToBuy);
						page.putField("shipping", shipping);
					}
				}
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			try {
				if(br!=null)
				br.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
		
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
