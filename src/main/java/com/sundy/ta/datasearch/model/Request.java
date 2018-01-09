package com.sundy.ta.datasearch.model;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import com.sundy.ta.datasearch.utils.UrlUtils;

import lombok.Data;

@Data
public class Request implements Serializable {
	
	private static final long serialVersionUID = -4226313927519101437L;

	public static final String CYCLE_TRIED_TIMES = "_cycle_tried_times";

    private String url;

    private String method;

    private HttpRequestBody requestBody;
    
    private Map<String, Object> extras = new HashMap<String, Object>();
    
    private Map<String, String> cookies = new HashMap<String, String>();

    private Map<String, String> headers = new HashMap<String, String>();
    
    private long priority;
    
    private String charset;
    
    private boolean binaryContent;
    
    private Site site;
    
    private String domain;

    public Request(String url) {
    	this.site = Site.me();
        site.setDomain(UrlUtils.getDomain(url));
        this.domain = site.getDomain();
        this.url = url;
    }
    
    public Request(String url, Site site) {
    	this.site = site;
        site.setDomain(UrlUtils.getDomain(url));
        this.domain = site.getDomain();
        this.url = url;
    }
    
    public Request setPriority(long priority) {
    	this.priority = priority;
    	return this;
    }

}
