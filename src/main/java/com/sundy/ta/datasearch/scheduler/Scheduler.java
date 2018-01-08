package com.sundy.ta.datasearch.scheduler;

import com.sundy.ta.datasearch.Task;
import com.sundy.ta.datasearch.model.Request;

public interface Scheduler {

	public void push(Request request, Task task);
	
	public Request poll(Task task);
	
	public long getLeftRequestsCount(Task task);

    public long getTotalRequestsCount(Task task);
	
}
