package com.sundy.ta.datasearch.scheduler;

import com.sundy.ta.datasearch.Task;
import com.sundy.ta.datasearch.model.Request;

public interface DuplicateRemover {

	public boolean isDuplicate(Request request, Task task);
	
	public void resetDuplicateCheck(Task task);
	
	public long getTotalRequestsCount(Task task);
	
}
