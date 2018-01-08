package com.sundy.ta.datasearch.pipeline;

import java.util.Map;

import com.sundy.ta.datasearch.Task;
import com.sundy.ta.datasearch.model.ResultItems;

public class ConsolePipeline implements Pipeline {

	@Override
	public void process(ResultItems resultItems, Task task) {
		for (Map.Entry<String, Object> entry: resultItems.getAll().entrySet()) {
			System.out.println("key="+entry.getKey()+"   value="+entry.getValue());
		}

	}

}
