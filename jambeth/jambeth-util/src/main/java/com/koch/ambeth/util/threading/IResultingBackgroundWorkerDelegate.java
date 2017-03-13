package com.koch.ambeth.util.threading;

public interface IResultingBackgroundWorkerDelegate<R>
{
	R invoke() throws Throwable;
}
