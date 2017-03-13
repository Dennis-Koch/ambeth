package com.koch.ambeth.ioc.threadlocal;

public interface IForkProcessor
{
	Object resolveOriginalValue(Object bean, String fieldName, ThreadLocal<?> fieldValueTL);

	Object createForkedValue(Object value);

	void returnForkedValue(Object value, Object forkedValue);
}
