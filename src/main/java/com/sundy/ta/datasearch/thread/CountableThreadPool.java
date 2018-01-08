package com.sundy.ta.datasearch.thread;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

public class CountableThreadPool {

	private final ExecutorService executorService;
	
	private ReentrantLock lock = new ReentrantLock();
	
	private Condition condition = lock.newCondition();
	
	private final int threadNum;
	
	private AtomicInteger currentThreads = new AtomicInteger();

	public CountableThreadPool(ExecutorService executorService, int threadNum) {
		this.threadNum = threadNum;
		this.executorService = executorService;
	}

	public CountableThreadPool(int threadNum) {
		this.threadNum = threadNum;
		this.executorService = Executors.newFixedThreadPool(threadNum);
	}
	
	public void execute(Runnable runnable){
		lock.lock();
		try {
			while(currentThreads.get()>=threadNum){
				try {
					condition.await();
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		} finally {
			lock.unlock();
		}
		
		currentThreads.incrementAndGet();
		executorService.execute(new Runnable() {
			@Override
			public void run() {
				try {
					runnable.run();
				} finally{
					try {
						lock.lock();
						currentThreads.decrementAndGet();
						condition.signal();
					} finally {
						lock.unlock();
					}
				}
			}
		});
		
	}
	
	public boolean isShutdown(){
		return executorService.isShutdown();
	}
	
	public void shutdown(){
		executorService.shutdown();
	}
	
	

	
	
	
	
}
