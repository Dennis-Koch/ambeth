package de.osthus.ambeth.threading;

public interface IResultingBackgroundWorkerParamDelegate<R, S>
{
	R invoke(S state) throws Throwable;
}
