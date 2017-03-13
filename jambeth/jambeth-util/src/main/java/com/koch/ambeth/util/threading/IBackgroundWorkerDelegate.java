package com.koch.ambeth.util.threading;

public interface IBackgroundWorkerDelegate
{
	void invoke() throws Throwable;
}
