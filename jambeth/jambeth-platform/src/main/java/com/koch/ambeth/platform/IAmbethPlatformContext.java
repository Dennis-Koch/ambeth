package com.koch.ambeth.platform;

import com.koch.ambeth.ioc.IServiceContext;
import com.koch.ambeth.util.IDisposable;

public interface IAmbethPlatformContext extends IDisposable
{
	IServiceContext getBeanContext();

	void clearThreadLocal();

	void afterBegin();

	void afterCommit();

	void afterRollback();
}
