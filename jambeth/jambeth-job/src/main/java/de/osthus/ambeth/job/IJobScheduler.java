package de.osthus.ambeth.job;

import java.util.Map;

public interface IJobScheduler
{
	void scheduleJob(Class<?> jobType, String cronPattern, Map<Object, Object> properties);

	void scheduleJob(String jobName, Class<?> jobType, String cronPattern, Map<Object, Object> properties);

	void scheduleJob(String jobName, Object jobTask, String cronPattern, Map<Object, Object> properties);

	void scheduleJob(String jobName, Object jobTask, String cronPattern, String username, char[] userpass, Map<Object, Object> properties);
}
