package com.koch.ambeth.util.threading;

public interface IBackgroundWorkerParamDelegate<T>
{
	void invoke(T state) throws Throwable;
}
