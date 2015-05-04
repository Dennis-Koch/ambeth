package de.osthus.ambeth.changecontroller;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;

import de.osthus.ambeth.collections.ArrayList;
import de.osthus.ambeth.collections.IList;
import de.osthus.ambeth.util.ParamChecker;

/**
 * This is a utility class that provides access to all new objects that should be merged with the database.
 */
public class CacheView
{

	// objects contains all new objects
	protected final List<Object> newObjects, oldObjects;

	// views contains a map from an interface to all objects whose class implements it. Entries are created lazily below.
	protected final HashMap<Class<?>, Collection<?>> newViews = new HashMap<Class<?>, Collection<?>>();
	// The entries in the oldViews lists correlate to those in the newViews list
	protected final HashMap<Class<?>, Collection<?>> oldViews = new HashMap<Class<?>, Collection<?>>();

	public CacheView(List<Object> newObjects, List<Object> oldObjects)
	{
		this.newObjects = newObjects;
		this.oldObjects = oldObjects;
	}

	/**
	 * Returns a list of new objects that have the given interface as type.
	 * 
	 * @param clazz
	 *            The interface that the object should implemented by the objects
	 * @return a list of new objects that implement the interface, never <code>null</code>
	 */
	@SuppressWarnings("unchecked")
	public <T> Collection<T> getNewObjectsOfClass(Class<T> clazz)
	{
		assureObjectsOfClass(clazz);
		return (Collection<T>) newViews.get(clazz);
	}

	/**
	 * Returns a list of new objects that have the given interface as type.
	 * 
	 * @param clazz
	 *            The interface that the object should implemented by the objects
	 * @return a list of new objects that implement the interface, never <code>null</code>
	 */
	@SuppressWarnings("unchecked")
	public <T> Collection<T> getOldObjectsOfClass(Class<T> clazz)
	{
		assureObjectsOfClass(clazz);
		return (Collection<T>) oldViews.get(clazz);
	}

	/**
	 * Make sure that all elements of the given class are computed
	 * 
	 * @param clazz
	 */
	protected void assureObjectsOfClass(Class<?> clazz)
	{
		// We just check for the entry in newViews because newViews and oldViews are updated simultaneously.
		if (!newViews.containsKey(clazz))
		{
			createObjectsOfClass(clazz);
		}
	}

	protected void createObjectsOfClass(Class<?> clazz)
	{
		ParamChecker.assertTrue(clazz.isInterface(), clazz.getName() + " is not an interface");

		IList<Object> newResult = new ArrayList<Object>();
		IList<Object> oldResult = new ArrayList<Object>();
		int size = newObjects.size();
		// Filter all objects that are an instance of the given class
		for (int index = 0; index < size; index += 1)
		{
			Object newEntity = newObjects.get(index);
			Object oldEntity = oldObjects.get(index);
			// just get one of both that is not null (there must be at least one)
			Object anyEntity = newEntity != null ? newEntity : oldEntity;
			if (clazz.isInstance(anyEntity))
			{
				newResult.add(newEntity);
				oldResult.add(oldEntity);
			}
		}
		newViews.put(clazz, newResult);
		oldViews.put(clazz, oldResult);
	}
}
