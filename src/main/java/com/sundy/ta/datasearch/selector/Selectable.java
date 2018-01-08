package com.sundy.ta.datasearch.selector;

import java.util.List;

public interface Selectable {

	public Selectable regex(String regex);
	
	public Selectable regex(String regex, int group);
	
	public List<String> all();
	
	public String get();
	
	public Selectable selectList(Selector selector);
	
	public Selectable select(Selector selector);
	
	public Selectable links();
	
}
