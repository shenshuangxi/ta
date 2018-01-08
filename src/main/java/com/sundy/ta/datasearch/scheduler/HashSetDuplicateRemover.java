package com.sundy.ta.datasearch.scheduler;

import java.util.HashSet;
import java.util.Set;

import com.sundy.ta.datasearch.Task;
import com.sundy.ta.datasearch.model.Request;

public class HashSetDuplicateRemover implements DuplicateRemover {

	private Set<String> urls = new HashSet<String>();
	
	@Override
	public boolean isDuplicate(Request request, Task task) {
		return !urls.add(request.getUrl());
	}

	@Override
	public void resetDuplicateCheck(Task task) {
		urls.clear();
	}

	@Override
	public long getTotalRequestsCount(Task task) {
		return urls.size();
	}

}
