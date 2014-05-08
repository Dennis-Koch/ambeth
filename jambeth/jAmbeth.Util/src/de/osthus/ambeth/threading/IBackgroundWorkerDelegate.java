package de.osthus.ambeth.threading;

public interface IBackgroundWorkerDelegate
{
	void invoke() throws Throwable;
}
