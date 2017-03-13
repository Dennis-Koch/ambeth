package com.koch.ambeth.util.objectcollector;

public interface ICollectableController
{
	Object createInstance() throws Throwable;

	void initObject(Object object) throws Throwable;

	void disposeObject(Object object) throws Throwable;
}
