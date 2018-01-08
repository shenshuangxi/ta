package com.sundy.ta.datasearch;

import com.sundy.ta.datasearch.model.Request;

public interface SpiderListener {

	public void onSuccess(Request request);

    public void onError(Request request);
	
}
