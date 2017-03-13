package com.koch.ambeth.ioc.threadlocal;

public interface IForkedValueResolver
{
	Object createForkedValue();

	Object getOriginalValue();
}
