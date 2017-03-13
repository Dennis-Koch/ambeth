package com.koch.ambeth.merge;

public interface IUnmodifiedObjectProvider
{

	Object getUnmodifiedObject(Class<?> type, Object id);

	Object getUnmodifiedObject(Object modifiedObject);

}
