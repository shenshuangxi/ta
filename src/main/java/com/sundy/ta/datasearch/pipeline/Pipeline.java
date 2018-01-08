package com.sundy.ta.datasearch.pipeline;

import com.sundy.ta.datasearch.Task;
import com.sundy.ta.datasearch.model.ResultItems;

public interface Pipeline {

	public void process(ResultItems resultItems, Task task);
	
}
