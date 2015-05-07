package de.osthus.ambeth.util;

public interface IAggregrateResultHandler<R, V>
{
	void aggregateResult(R resultOfFork, V itemOfFork) throws Throwable;
}
