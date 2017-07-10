package com.koch.ambeth.mina.client;

/*-
 * #%L
 * jambeth-mina
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

import org.apache.mina.core.session.IdleStatusChecker;
import org.apache.mina.core.session.IdleStatusChecker.NotifyingTask;

import com.koch.ambeth.ioc.IDisposableBean;
import com.koch.ambeth.ioc.IInitializingBean;
import com.koch.ambeth.ioc.annotation.Autowired;

public class IdleStatusCheckerShutdownHook implements IDisposableBean, IInitializingBean {
	@Autowired
	protected IdleStatusChecker idleStatusChecker;

	private NotifyingTask notifyingTask;

	@Override
	public void afterPropertiesSet() throws Throwable {
		notifyingTask = idleStatusChecker.getNotifyingTask();
		Thread thread = new Thread(notifyingTask);
		thread.setDaemon(true);
		thread.setName("IdleStatusChecker");
		thread.start();
	}

	@Override
	public void destroy() throws Throwable {
		// stop the idle checking task
		notifyingTask.cancel();
	}
}
