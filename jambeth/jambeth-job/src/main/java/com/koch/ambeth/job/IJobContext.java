package com.koch.ambeth.job;

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
