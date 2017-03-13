package com.koch.ambeth.util.threading;

public interface IResultingBackgroundWorkerParamDelegate<R, S>
{
	R invoke(S state) throws Throwable;
}
