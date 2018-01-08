package com.sundy.ta.datasearch.pageprocessor;

import com.sundy.ta.datasearch.model.Page;
import com.sundy.ta.datasearch.model.Site;

public class EbayPageProcessor implements PageProcessor {

	private Site site = Site.me().setRetryTimes(3).setSleepTime(100);
	
	public void process(Page page) {
		page.addTargetRequests(page.getHtml().links().all());
	}

	public Site getSite() {
		return site;
	}
	
}