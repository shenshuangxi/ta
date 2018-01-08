package com.sundy.ta.datasearch.selector;

import java.util.ArrayList;
import java.util.List;

public class PlainText extends AbstractSelectable {

	private List<String> sourceTexts;
	
	public PlainText(List<String> sourceTexts) {
        this.sourceTexts = sourceTexts;
    }

    public PlainText(String text) {
        this.sourceTexts = new ArrayList<String>();
        sourceTexts.add(text);
    }
	
	@Override
	protected List<String> getSourceTexts() {
		return sourceTexts;
	}
	
	@Override
    public Selectable links() {
		throw new UnsupportedOperationException("Links can not apply to plain text. Please check whether you use a previous xpath with attribute select (/@href etc).");
    }

	

}
