package com.sundy.ta.datasearch.model;

import org.apache.commons.lang3.StringUtils;

import com.sundy.ta.datasearch.constant.Constant;
import com.sundy.ta.datasearch.selector.Html;
import com.sundy.ta.datasearch.selector.Selectable;
import com.sundy.ta.datasearch.utils.UrlUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class Page {

    private Request request;

    private ResultItems resultItems = new ResultItems();

    private String rawText;

    private String url;
    
    private Html html;

    private Map<String,List<String>> headers;

    private int statusCode = Constant.StatusCode.OK.getCode();

    private boolean downloadSuccess = true;

    private byte[] bytes;

    private List<Request> targetRequests = new ArrayList<Request>();

    private String charset;
    
    public Page() {
    }

    public static Page fail(){
        Page page = new Page();
        page.setDownloadSuccess(false);
        return page;
    }

    public Page setSkip(boolean skip) {
        resultItems.setSkip(skip);
        return this;

    }

    public void putField(String key, Object field) {
        resultItems.put(key, field);
    }

    public List<Request> getTargetRequests() {
        return targetRequests;
    }

    public void addTargetRequests(List<Request> requests) {
        for (Request request : requests) {
            if (StringUtils.isBlank(request.getUrl()) || request.getUrl().equals("#") || request.getUrl().startsWith("javascript:")) {
                continue;
            }
            request.setUrl(UrlUtils.canonicalizeUrl(request.getUrl(), url.toString()));
            targetRequests.add(request);
        }
    }

    public void addTargetRequests(List<Request> requests, long priority) {
    	for (Request request : requests) {
            if (StringUtils.isBlank(request.getUrl()) || request.getUrl().equals("#") || request.getUrl().startsWith("javascript:")) {
                continue;
            }
            request.setUrl(UrlUtils.canonicalizeUrl(request.getUrl(), url.toString()));
            targetRequests.add(request.setPriority(priority));
        }
    }

    public void addTargetRequest(Request request) {
        targetRequests.add(request);
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public Request getRequest() {
        return request;
    }

    public void setRequest(Request request) {
        this.request = request;
        this.resultItems.setRequest(request);
    }

    public ResultItems getResultItems() {
        return resultItems;
    }

    public int getStatusCode() {
        return statusCode;
    }

    public void setStatusCode(int statusCode) {
        this.statusCode = statusCode;
    }

    public String getRawText() {
        return rawText;
    }

    public Page setRawText(String rawText) {
        this.rawText = rawText;
        return this;
    }

    public Map<String, List<String>> getHeaders() {
        return headers;
    }

    public void setHeaders(Map<String, List<String>> headers) {
        this.headers = headers;
    }

    public boolean isDownloadSuccess() {
        return downloadSuccess;
    }

    public void setDownloadSuccess(boolean downloadSuccess) {
        this.downloadSuccess = downloadSuccess;
    }

    public byte[] getBytes() {
        return bytes;
    }

    public void setBytes(byte[] bytes) {
        this.bytes = bytes;
    }

    public String getCharset() {
        return charset;
    }

    public void setCharset(String charset) {
        this.charset = charset;
    }
    
    public Selectable getHtml() {
    	if (html == null) {
            html = new Html(rawText, request.getUrl());
        }
    	return html;
	}

	@Override
    public String toString() {
        return "Page{" +
                "request=" + request +
                ", resultItems=" + resultItems +
                ", rawText='" + rawText + '\'' +
                ", url=" + url +
                ", headers=" + headers +
                ", statusCode=" + statusCode +
                ", downloadSuccess=" + downloadSuccess +
                ", targetRequests=" + targetRequests +
                ", charset='" + charset + '\'' +
                ", bytes=" + Arrays.toString(bytes) +
                '}';
    }

}
