package de.osthus.ambeth.objectcollector;

public interface ICollectableController
{
	Object createInstance() throws Throwable;

	void initObject(Object object) throws Throwable;

	void disposeObject(Object object) throws Throwable;
}
