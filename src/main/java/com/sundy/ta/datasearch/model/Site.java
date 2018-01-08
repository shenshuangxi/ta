package com.sundy.ta.datasearch.model;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import com.sundy.ta.datasearch.Task;
import com.sundy.ta.datasearch.constant.Constant;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class Site {

	private String userAgent = "Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/56.0.2924.87 Safari/537.36";
	
	private boolean useGzip = true;
	
	private Integer timeOut = 5000;
	
	private Integer retryTimes = 0;
	
	private int cycleRetryTimes = 0;

    private int retrySleepTime = 1000;
	
	private Integer sleepTime = 5000;
	
	private boolean disableCookieManagement = false;
	
	private String domain;
	
	private String charset;
	
	private Map<String, String> defaultCookies = new HashMap<String, String>();
	
	private Map<String, Map<String, String>> cookies = new HashMap<String, Map<String,String>>();
	
	private Map<String, String> headers = new HashMap<String, String>();
	
	private static final Set<Integer> DEFAULT_STATUS_CODE_SET = new HashSet<Integer>();
	
	private Set<Integer> acceptStatCode = DEFAULT_STATUS_CODE_SET;
	
	static {
        DEFAULT_STATUS_CODE_SET.add(Constant.StatusCode.OK.getCode());
    }

	public static Site me() {
		return new Site();
	}
	
	public Site setCharset(String charset) {
		this.charset = charset;
		return this;
	}
	
	public Site setUserAgent(String userAgent) {
		this.userAgent = userAgent;
		return this;
	}

	public Site setUseGzip(boolean useGzip) {
		this.useGzip = useGzip;
		return this;
	}

	public Site setTimeOut(Integer timeOut) {
		this.timeOut = timeOut;
		return this;
	}

	public Site setRetryTimes(Integer retryTimes) {
		this.retryTimes = retryTimes;
		return this;
	}

	public Site setDisableCookieManagement(boolean disableCookieManagement) {
		this.disableCookieManagement = disableCookieManagement;
		return this;
	}

	public Site setDomain(String domain) {
		this.domain = domain;
		return this;
	}

	public Site setDefaultCookies(Map<String, String> defaultCookies) {
		this.defaultCookies = defaultCookies;
		return this;
	}

	public Site setCookies(Map<String, Map<String, String>> cookies) {
		this.cookies = cookies;
		return this;
	}

	public Site setHeaders(Map<String, String> headers) {
		this.headers = headers;
		return this;
	}
	
	public Site setSleepTime(Integer sleepTime) {
		this.sleepTime = sleepTime;
		return this;
	}
	
	public Task toTask() {
        return new Task() {
            @Override
            public String getUUID() {
                String uuid = Site.this.getDomain();
                if (uuid == null) {
                    uuid = UUID.randomUUID().toString();
                }
                return uuid;
            }
            @Override
            public Site getSite() {
                return Site.this;
            }
        };
    }

	
	

}
