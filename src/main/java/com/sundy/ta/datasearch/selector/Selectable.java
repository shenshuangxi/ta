package com.sundy.ta.datasearch.selector;

import java.util.List;
import java.util.function.Function;

public interface Selectable {

    public Selectable $(String selector);

    public Selectable $(String selector, String attrName);

    public Selectable css(String selector);

    public Selectable css(String selector, String attrName);
    
    public List<Selectable> nodes();
	
	public Selectable regex(String regex);
	
	public Selectable regex(String regex, int group);
	
	public List<String> all();
	
	public String get();
	
	public Selectable selectList(Selector selector);
	
	public Selectable select(Selector selector);
	
	public Selectable links();

}
