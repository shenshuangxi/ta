package com.sundy.ta.datasearch.scheduler;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sundy.ta.datasearch.Task;
import com.sundy.ta.datasearch.constant.Constant;
import com.sundy.ta.datasearch.model.Request;

public abstract class DuplicateRemovedScheduler implements Scheduler {

	private final Logger logger = LoggerFactory.getLogger(DuplicateRemovedScheduler.class);
	
	private DuplicateRemover duplicateRemover;
	
	public DuplicateRemovedScheduler(DuplicateRemover duplicateRemover) {
		this.duplicateRemover = duplicateRemover;
	}

	@Override
	public void push(Request request, Task task) {
		logger.trace("get a candidate url {}", request.getUrl());
        if (shouldReserved(request) || noNeedToRemoveDuplicate(request) || !duplicateRemover.isDuplicate(request, task)) {
            logger.debug("push to queue {}", request.getUrl());
            pushWhenNoDuplicate(request, task);
        }
	}

	protected boolean shouldReserved(Request request) {
		return request.getExtras().get(Request.CYCLE_TRIED_TIMES) != null;
	}
	
	protected boolean noNeedToRemoveDuplicate(Request request) {
		return Constant.Method.POST.getCode().equalsIgnoreCase(request.getMethod());
	}
	
	protected abstract void pushWhenNoDuplicate(Request request, Task task);

	@Override
	public long getTotalRequestsCount(Task task) {
		return duplicateRemover.getTotalRequestsCount(task);
	}
	
	
	
	
}
