package com.sundy.ta.datasearch;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

import org.apache.commons.lang3.SerializationUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sundy.ta.datasearch.constant.Constant;
import com.sundy.ta.datasearch.downloader.Downloader;
import com.sundy.ta.datasearch.downloader.HttpClientDownloader;
import com.sundy.ta.datasearch.model.Page;
import com.sundy.ta.datasearch.model.Request;
import com.sundy.ta.datasearch.pageprocessor.PageProcessor;
import com.sundy.ta.datasearch.pipeline.ConsolePipeline;
import com.sundy.ta.datasearch.pipeline.Pipeline;
import com.sundy.ta.datasearch.scheduler.QueueScheduler;
import com.sundy.ta.datasearch.scheduler.Scheduler;
import com.sundy.ta.datasearch.thread.CountableThreadPool;

public class Spider implements Runnable, Task {
	
	private final static Logger logger = LoggerFactory.getLogger(Spider.class);

	private Downloader downloader;
	
	private List<PageProcessor> pageProcessors = new ArrayList<PageProcessor>();
	
	private Scheduler scheduler;
	
	private List<Pipeline> pipelines = new ArrayList<Pipeline>();
	
	private Request initRequest;
	
	private List<SpiderListener> spiderListeners = new ArrayList<SpiderListener>();
	
	private int threadNum;
	
	private CountableThreadPool threadPool;
	
	private Date startDate;
	
	private Constant.State state = Constant.State.INIT;
	
	private ReentrantLock newUrlLock = new ReentrantLock();
	
	private Condition newUrlCondition = newUrlLock.newCondition();
	
	private final AtomicLong pageCount = new AtomicLong(0);
	
	private final String taskName;
	
	private Spider(String taskName, String domain) {
		this.taskName = taskName;
	}

	public Spider thread(int threadNum) {
		this.threadNum = threadNum;
		return this;
	}
	
	public Spider addPipeLine(Pipeline pipeline){
		pipelines.add(pipeline);
		return this;
	}
	
	public Spider addSpiderListener(SpiderListener spiderListener) {
		spiderListeners.add(spiderListener);
		return this;
	}
	
	public Spider addPageProcessor(PageProcessor pageProcessor) {
		this.pageProcessors.add(pageProcessor);
		return this;
	}
	
	public Spider setDownloader(Downloader downloader) {
		this.downloader = downloader;
		return this;
	}
	
	public Spider setRequest(Request request) {
		this.initRequest = request;
		return this;
	}
	
	public Spider setScheduler(Scheduler scheduler) {
		this.scheduler = scheduler;
		return this;
	}

	public Date getStartDate() {
		return startDate;
	}

	public void initComponent() {
		if(downloader==null) {
			downloader = new HttpClientDownloader();
		}
		
		if(scheduler == null) {
			scheduler = new QueueScheduler();
		}
		
		if(pipelines.isEmpty()) {
			pipelines.add(new ConsolePipeline());
		}
		
		downloader.setThread(threadNum);
		
		if(threadPool==null) {
			threadPool = new CountableThreadPool(threadNum);
		}

		if(initRequest != null) {
			addRequest(initRequest);
		}
		startDate = new Date();
	}

	@Override
	public void run() {
		checkIfRunning();
		initComponent();
		logger.info("Spider {} started!",getTaskName());
		while(!Thread.currentThread().isInterrupted() && this.state.getCode()==Constant.State.RUNNING.getCode()) {
			final Request request = scheduler.poll(this);
			if(request==null) {
				waitNewUrl();
			} else {
				threadPool.execute(new Runnable() {
					@Override
					public void run() {
						try {
							processRequest(request);
							onSuccess(request);
						} catch (Exception e) {
							onError(request);
	                        logger.error("process request " + request + " error", e);
						} finally {
							pageCount.incrementAndGet();
						}
					}
				});
			}
		}
		close();
	}
	
	public void close() {
		this.state = Constant.State.STOP;
		threadPool.shutdown();
	}

	private void onError(Request request) {
		for (SpiderListener spiderListener : spiderListeners) {
            spiderListener.onError(request);
        }
	}
	
	
	private static long startTime = System.currentTimeMillis(); 
	private static long endTime = System.currentTimeMillis();
	private AtomicLong pagesDownloaders = new AtomicLong();
	
	private void processRequest(Request request) {
		Page page = downloader.download(request);
		if(page.isDownloadSuccess()) {
			downloadSuccess(request, page);
		} else {
			downloadError(request);
		}
		pagesDownloaders.incrementAndGet();
		endTime = System.currentTimeMillis();
		logger.error("耗时: "+(endTime-startTime)+"ms 下载页面: "+ pagesDownloaders.get());
	}
	
	private void downloadError(Request request) {
		if (request.getSite().getCycleRetryTimes() == 0) {
            sleep(request.getSite().getSleepTime());
        } else {
            // for cycle retry
            doCycleRetry(request);
        }
	}
	
	private void doCycleRetry(Request request) {
        Object cycleTriedTimesObject = request.getExtras().get(Request.CYCLE_TRIED_TIMES);
        if (cycleTriedTimesObject == null) {
            Request cloneRequest = SerializationUtils.clone(request);
        	cloneRequest.setPriority(0).getExtras().put(Request.CYCLE_TRIED_TIMES, 1);
            addRequest(cloneRequest);
        } else {
            int cycleTriedTimes = (Integer) cycleTriedTimesObject;
            cycleTriedTimes++;
            if (cycleTriedTimes < request.getSite().getCycleRetryTimes()) {
            	Request cloneRequest = SerializationUtils.clone(request);
            	cloneRequest.setPriority(0).getExtras().put(Request.CYCLE_TRIED_TIMES, cycleTriedTimes);
                addRequest(cloneRequest);
            }
        }
        sleep(request.getSite().getRetrySleepTime());
    }

	private void downloadSuccess(Request request, Page page) {
		if(request.getSite().getAcceptStatCode().contains(page.getStatusCode())) {
			for (PageProcessor pageProcessor : pageProcessors) {
				pageProcessor.process(page);
			}
			extractAndAddRequests(page);
			if (!page.getResultItems().isSkip()) {
                for (Pipeline pipeline : pipelines) {
                    pipeline.process(page.getResultItems(), this);
                }
            }
		}
		
	}
	
	protected void sleep(int time) {
        try {
            Thread.sleep(time);
        } catch (InterruptedException e) {
            logger.error("Thread interrupted when sleep",e);
        }
    }

	private void extractAndAddRequests(Page page) {
		addRequests(page.getTargetRequests());
	}

	protected void onSuccess(Request request) {
		for (SpiderListener spiderListener : spiderListeners) {
			spiderListener.onSuccess(request);
		}
	}
	
	private void addRequest(Request request) {
		scheduler.push(request, this);
		signalNewUrl();
	}
	
	private void addRequests(List<Request> requests) {
		boolean hasNewUrl = false;
		for (Request request : requests) {
			scheduler.push(request, this);
			hasNewUrl = true;
		}
		if(hasNewUrl) {
			signalNewUrl();
		}
	}

	private void waitNewUrl() {
		newUrlLock.lock();
		try {
			try {
				newUrlCondition.await();
			} catch (InterruptedException e) {
				logger.warn("waitNewUrl - interrupted, error {}", e);
			}
		} finally {
			newUrlLock.unlock();
		}
	}
	
	private void signalNewUrl() {
        try {
            newUrlLock.lock();
            newUrlCondition.signalAll();
        } finally {
            newUrlLock.unlock();
        }
    }

	private void checkIfRunning() {
		if(state.getCode() == Constant.State.RUNNING.getCode()) {
			throw new IllegalStateException("Spider is already running!");
		} else {
			state = Constant.State.RUNNING;
		}
	}

	public static Spider create(String taskName, String domain) {
		Spider spider = new Spider(taskName, domain);
		return spider;
	}

	@Override
	public String getTaskName() {
		return taskName;
	}

}
