package com.koch.ambeth.job;

public interface IJobExtendable
{
	void registerJob(IJob job, String jobName, String cronPattern);

	void unregisterJob(IJob job, String jobName, String cronPattern);
}
