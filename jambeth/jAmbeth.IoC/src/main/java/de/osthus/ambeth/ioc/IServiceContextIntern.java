package de.osthus.ambeth.ioc;

public interface IServiceContextIntern extends IServiceContext
{
	void childContextDisposed(IServiceContext childContext);
}
