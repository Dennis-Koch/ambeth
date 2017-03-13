package com.osthus.ambeth.mina.client;

import org.apache.mina.core.session.IdleStatusChecker;
import org.apache.mina.core.session.IdleStatusChecker.NotifyingTask;

import com.koch.ambeth.ioc.IDisposableBean;
import com.koch.ambeth.ioc.IInitializingBean;
import com.koch.ambeth.ioc.annotation.Autowired;
import com.koch.ambeth.log.ILogger;
import com.koch.ambeth.log.LogInstance;

public class IdleStatusCheckerShutdownHook implements IDisposableBean, IInitializingBean
{
	@SuppressWarnings("unused")
	@LogInstance
	private ILogger log;

	@Autowired
	protected IdleStatusChecker idleStatusChecker;

	private NotifyingTask notifyingTask;

	@Override
	public void afterPropertiesSet() throws Throwable
	{
		notifyingTask = idleStatusChecker.getNotifyingTask();
		Thread thread = new Thread(notifyingTask);
		thread.setDaemon(true);
		thread.setName("IdleStatusChecker");
		thread.start();
	}

	@Override
	public void destroy() throws Throwable
	{
		// stop the idle checking task
		notifyingTask.cancel();
	}
}
