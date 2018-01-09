package com.sundy.ta.datasearch.selector;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;


public abstract class AbstractSelectable implements Selectable {

	protected abstract List<String> getSourceTexts();
	
	@Override
    public Selectable css(String selector) {
        return $(selector);
    }

    @Override
    public Selectable css(String selector, String attrName) {
        return $(selector, attrName);
    }
	
	
	@Override
	public Selectable regex(String regex) {
		RegexSelector regexSelector = new RegexSelector(regex);
		return selectList(regexSelector, getSourceTexts());
	}

	@Override
	public Selectable regex(String regex, int group) {
		RegexSelector regexSelector = new RegexSelector(regex, group);
		return selectList(regexSelector, getSourceTexts());
	}

	private Selectable selectList(Selector selector, List<String> sourceTexts) {
		List<String> results = new ArrayList<String>();
		for (String sourceText : sourceTexts) {
			List<String> result = selector.selectList(sourceText);
			results.addAll(result);
		}
		return new PlainText(results);
	}
	
	private Selectable select(Selector selector, List<String> sourceTexts) {
		List<String> results = new ArrayList<String>();
		for (String sourceText : sourceTexts) {
			String result = selector.select(sourceText);
			results.add(result);
		}
		return new PlainText(results);
	}

	@Override
	public List<String> all() {
		return getSourceTexts();
	}

	@Override
	public Selectable selectList(Selector selector) {
		return selectList(selector, getSourceTexts());
	}

	@Override
	public Selectable select(Selector selector) {
		return select(selector, getSourceTexts());
	}

	@Override
	public String get() {
		List<String> results = all();
		if (results!=null && !results.isEmpty()) {
            return results.get(0);
        } else {
            return null;
        }
	}


}
