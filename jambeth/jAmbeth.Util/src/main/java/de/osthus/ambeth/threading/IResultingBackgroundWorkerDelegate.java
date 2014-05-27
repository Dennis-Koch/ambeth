package de.osthus.ambeth.threading;

public interface IResultingBackgroundWorkerDelegate<R>
{
	R invoke() throws Throwable;
}
