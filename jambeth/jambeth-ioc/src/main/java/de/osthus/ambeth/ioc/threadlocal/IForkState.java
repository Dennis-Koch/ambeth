package de.osthus.ambeth.ioc.threadlocal;

public interface IForkState
{
	void use(Runnable runnable);
}
