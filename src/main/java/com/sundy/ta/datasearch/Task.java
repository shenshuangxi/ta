package com.sundy.ta.datasearch;

public interface Task {

	/**
     * unique id for a task.
     *
     * @return uuid
     */
    public String getTaskName();
    
    public String getDomain();

}
