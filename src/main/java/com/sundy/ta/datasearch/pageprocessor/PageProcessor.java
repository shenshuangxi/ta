package com.sundy.ta.datasearch.pageprocessor;

import com.sundy.ta.datasearch.model.Page;
import com.sundy.ta.datasearch.model.Site;

public interface PageProcessor {

	public void process(Page page);
	
	public Site getSite();
	
}
