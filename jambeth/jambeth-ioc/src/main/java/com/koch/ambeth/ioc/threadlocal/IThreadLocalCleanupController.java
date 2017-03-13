package com.koch.ambeth.ioc.threadlocal;

public interface IThreadLocalCleanupController
{
	void cleanupThreadLocal();

	IForkState createForkState();
}