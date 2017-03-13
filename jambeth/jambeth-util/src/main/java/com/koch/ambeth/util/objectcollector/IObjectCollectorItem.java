package com.koch.ambeth.util.objectcollector;

public interface IObjectCollectorItem
{
	Object getOneInstance();

	void dispose(Object object);

	void cleanUp();

}