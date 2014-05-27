package de.osthus.ambeth.ioc.threadlocal;

public interface IThreadLocalCleanupBeanExtendable
{
	void registerThreadLocalCleanupBean(IThreadLocalCleanupBean threadLocalCleanupBean);

	void unregisterThreadLocalCleanupBean(IThreadLocalCleanupBean threadLocalCleanupBean);
}