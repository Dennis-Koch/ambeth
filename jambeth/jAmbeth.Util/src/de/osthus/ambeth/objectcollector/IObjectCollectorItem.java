package de.osthus.ambeth.objectcollector;

public interface IObjectCollectorItem
{
	Object getOneInstance();

	void dispose(Object object);

	void cleanUp();

}