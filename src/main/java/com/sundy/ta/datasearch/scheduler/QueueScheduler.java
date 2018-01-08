package com.sundy.ta.datasearch.scheduler;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicLong;

import com.sundy.ta.datasearch.Task;
import com.sundy.ta.datasearch.model.Request;

public class QueueScheduler extends DuplicateRemovedScheduler {

	private Map<String, ConcurrentLinkedQueue<Request>> requests = new ConcurrentHashMap<String, ConcurrentLinkedQueue<Request>>();
	
	private AtomicLong count = new AtomicLong();
	
	public QueueScheduler() {
		super(new HashSetDuplicateRemover());
	}

	public QueueScheduler(DuplicateRemover duplicateRemover) {
		super(duplicateRemover);
	}

	@Override
	public Request poll(Task task) {
		if(requests.containsKey(task.getUUID())) {
			Request request = requests.get(task.getUUID()).poll();
			if (request!=null) {
				count.decrementAndGet();
				return request;
			}
		} 
		return null;
	}

	@Override
	public long getLeftRequestsCount(Task task) {
		return count.get();
	}

	@Override
	protected void pushWhenNoDuplicate(Request request, Task task) {
		if(requests.containsKey(task.getUUID())) {
			requests.get(task.getUUID()).add(request);
		} else {
			ConcurrentLinkedQueue<Request> queue = new ConcurrentLinkedQueue<Request>();
			queue.add(request);
			requests.put(task.getUUID(), queue);
		}
		count.incrementAndGet();
	}
	
	

}
