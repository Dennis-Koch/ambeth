package de.osthus.ambeth.changecontroller;

import java.util.Collection;

import de.osthus.ambeth.threading.IBackgroundWorkerDelegate;

/**
 * This is a utility class that provides access to all new objects that should be merged with the database.
 */
public interface ICacheView
{

	/**
	 * Returns a list of new objects that have the given interface as type.
	 * 
	 * @param clazz
	 *            The interface that the object should implemented by the objects
	 * @return a list of new objects that implement the interface, never <code>null</code>
	 */
	<T> Collection<T> getNewObjectsOfClass(Class<T> clazz);

	/**
	 * Returns a list of new objects that have the given interface as type.
	 * 
	 * @param clazz
	 *            The interface that the object should implemented by the objects
	 * @return a list of new objects that implement the interface, never <code>null</code>
	 */
	<T> Collection<T> getOldObjectsOfClass(Class<T> clazz);

	Object getCustomState(Object key);

	void setCustomState(Object key, Object value);

	void queueRunnable(IBackgroundWorkerDelegate runnable);

}