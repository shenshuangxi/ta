package com.sundy.ta.datasearch;

import com.sundy.ta.datasearch.model.Site;

public interface Task {

	/**
     * unique id for a task.
     *
     * @return uuid
     */
    public String getUUID();

    /**
     * site of a task
     *
     * @return site
     */
    public Site getSite();
	
}
