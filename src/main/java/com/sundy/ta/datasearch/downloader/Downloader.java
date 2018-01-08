package com.sundy.ta.datasearch.downloader;

import com.sundy.ta.datasearch.Task;
import com.sundy.ta.datasearch.model.Page;
import com.sundy.ta.datasearch.model.Request;

public interface Downloader {

    public Page download(Request request, Task task);

    public void setThread(int threadNum);
    
    public void onSuccess(Request request);
    
    public void onError(Request request);
}
