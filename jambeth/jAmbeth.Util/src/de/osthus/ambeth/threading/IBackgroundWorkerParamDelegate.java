package de.osthus.ambeth.threading;

public interface IBackgroundWorkerParamDelegate<T>
{
	void invoke(T state) throws Throwable;
}
