package com.koch.ambeth.job;

/*-
 * #%L
 * jambeth-job
 * %%
 * Copyright (C) 2017 Koch Softwaredevelopment
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

     http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
 * #L%
 */

public interface IJobContext
{
	/**
	 * Sets the current status tracking message, that has to be something about what the task is doing at the moment.
	 * 
	 * @param message
	 *            A message representing the current execution status. Null messages will be blanked.
	 */
	void setStatusMessage(String message);

	/**
	 * Sets the completeness tracking value, that has to be between 0 and 1.
	 * 
	 * @param completeness
	 *            A completeness value, between 0 and 1. Values out of range will be ignored.
	 */
	void setCompleteness(double completeness);

	/**
	 * If the task execution has been paused, stops until the operation is resumed. It can also returns because of a stop operation without any previous
	 * resuming. Due to this the task developer should always check the {@link IJobContext#isStopped()} value after any <em>pauseIfRequested()</em> call. Note
	 * that a task execution can be paused only if the task {@link IJob#canBePaused()} method returns <em>true</em>.
	 */
	void pauseIfRequested();

	/**
	 * Checks whether the task execution has been demanded to be stopped. If the returned value is <em>true</em>, the task developer must shut down gracefully
	 * its task execution, as soon as possible. Note that a task execution can be stopped only if the task {@link IJob#canBePaused()} method returns
	 * <em>true</em>.
	 * 
	 * @return <em>true</em> if the current task execution has been demanded to be stopped; <em>false</em> otherwise.
	 */
	boolean isStopped();
}
