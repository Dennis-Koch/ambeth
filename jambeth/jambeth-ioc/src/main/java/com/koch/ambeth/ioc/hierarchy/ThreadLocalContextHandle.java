package com.koch.ambeth.ioc.hierarchy;

/*-
 * #%L
 * jambeth-ioc
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

import com.koch.ambeth.ioc.IServiceContext;
import com.koch.ambeth.ioc.annotation.Autowired;
import com.koch.ambeth.ioc.threadlocal.Forkable;
import com.koch.ambeth.ioc.threadlocal.IThreadLocalCleanupBean;
import com.koch.ambeth.ioc.threadlocal.IThreadLocalCleanupBeanExtendable;
import com.koch.ambeth.log.ILogger;
import com.koch.ambeth.log.LogInstance;
import com.koch.ambeth.util.threading.SensitiveThreadLocal;

public class ThreadLocalContextHandle extends AbstractChildContextHandle
		implements IThreadLocalCleanupBean {
	@SuppressWarnings("unused")
	@LogInstance
	private ILogger log;

	@Forkable
	protected final ThreadLocal<IServiceContext> childContextTL =
			new SensitiveThreadLocal<>();

	@Autowired
	protected IThreadLocalCleanupBeanExtendable threadLocalCleanupBeanExtendable;

	@Override
	public void afterPropertiesSet() throws Throwable {
		super.afterPropertiesSet();

		threadLocalCleanupBeanExtendable.registerThreadLocalCleanupBean(this);
	}

	@Override
	public void destroy() throws Throwable {
		threadLocalCleanupBeanExtendable.unregisterThreadLocalCleanupBean(this);
		super.destroy();
	}

	@Override
	protected IServiceContext getChildContext() {
		return childContextTL.get();
	}

	@Override
	protected void setChildContext(IServiceContext childContext) {
		childContextTL.set(childContext);
	}

	@Override
	public void cleanupThreadLocal() {
		IServiceContext context = childContextTL.get();
		if (context == null) {
			return;
		}
		childContextTL.remove();
		context.dispose();
	}
}
