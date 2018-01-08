package com.sundy.ta.datasearch.selector;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class RegexSelector implements Selector {

	private String regexStr;
	
	private Pattern regex;
	
	private int group;
	
	public RegexSelector(String regexStr, int group) {
        this.compileRegex(regexStr);
        this.group = group;
    }
	
	public RegexSelector(String regexStr) {
        this.compileRegex(regexStr);
        if(regex.matcher("").groupCount() == 0) {
        	this.group = 0;
        } else {
        	this.group = 1;
        }
    }
	
	private void compileRegex(String regexStr) {
		try {
			this.regexStr = regexStr;
			regex = Pattern.compile(regexStr, Pattern.DOTALL | Pattern.CASE_INSENSITIVE);
		} catch (Exception e) {
			throw new IllegalArgumentException("invalid regex "+regexStr, e);
		}
	}

	@Override
	public String select(String text) {
		return selectGroup(text).get(group);
	}

	private RegexResult selectGroup(String text) {
		Matcher matcher = regex.matcher(text);
		if(matcher.find()) {
			String[] groups = new String[matcher.groupCount()+1];
			for(int i=0; i<groups.length;i++){
				groups[i] = matcher.group(i);
			}
			return new RegexResult(groups);
		}
		return RegexResult.EMPTY_RESULT;
	}

	@Override
	public List<String> selectList(String text) {
		List<String> resultList = new ArrayList<String>();
		List<RegexResult> results = selectGroupList(text);
		for (RegexResult regexResult : results) {
			String result = regexResult.get(group);
			resultList.add(result);
		}
		return resultList;
	}
	
	private List<RegexResult> selectGroupList(String text) {
		List<RegexResult> resultList = new ArrayList<RegexResult>();
		Matcher matcher = regex.matcher(text);
		while(matcher.find()) {
			String[] groups = new String[matcher.groupCount()+1];
			for(int i=0; i<groups.length;i++){
				groups[i] = matcher.group(i);
			}
			resultList.add(new RegexResult(groups));
		}
		return resultList;
	}
	
	@Override
    public String toString() {
        return regexStr;
    }

}
