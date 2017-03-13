package com.koch.ambeth.ioc.util;

public interface IAggregrateResultHandler<R, V>
{
	void aggregateResult(R resultOfFork, V itemOfFork) throws Throwable;
}
