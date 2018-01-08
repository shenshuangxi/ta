package com.sundy.ta.datasearch.pageprocessor;

import com.sundy.ta.datasearch.model.Page;
import com.sundy.ta.datasearch.model.Site;

public class EbayPageProcessor implements PageProcessor {

	private Site site = Site.me().setRetryTimes(3).setSleepTime(100).setUserAgent("Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/56.0.2924.87 Safari/537.36");
	
	public void process(Page page) {
		page.addTargetRequests(page.getHtml().links().all());
	}

	public Site getSite() {
		return site;
	}
	
}
