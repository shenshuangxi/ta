package com.sundy.ta.datasearch.proxy;

import com.sundy.ta.datasearch.Task;
import com.sundy.ta.datasearch.model.Page;
import com.sundy.ta.datasearch.model.Request;

/**
 * Proxy provider. <br>
 *     
 * @since 0.7.0
 */
public interface ProxyProvider {

    /**
     *
     * Return proxy to Provider when complete a download.
     * @param proxy the proxy config contains host,port and identify info
     * @param page the download result
     * @param request the download request
     */
    void returnProxy(Proxy proxy, Page page, Request request);

    /**
     * Get a proxy for task by some strategy.
     * @param request the download request
     * @return proxy 
     */
    Proxy getProxy(Request request);
    
}
