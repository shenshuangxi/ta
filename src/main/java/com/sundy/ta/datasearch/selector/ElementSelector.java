package com.sundy.ta.datasearch.selector;

import java.util.List;

import org.jsoup.nodes.Element;

public interface ElementSelector {

	public String select(Element element);
	
	public List<String> selectList(Element element);
	
}
