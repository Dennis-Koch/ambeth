package com.koch.ambeth.merge.copy;

/*-
 * #%L
 * jambeth-merge
 * %%
 * Copyright (C) 2017 Koch Softwaredevelopment
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

     http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
 * #L%
 */

import java.lang.reflect.Array;
import java.util.Collection;

import com.koch.ambeth.ioc.annotation.Autowired;
import com.koch.ambeth.ioc.extendable.ClassExtendableContainer;
import com.koch.ambeth.ioc.threadlocal.IThreadLocalCleanupBean;
import com.koch.ambeth.ioc.util.ImmutableTypeSet;
import com.koch.ambeth.util.collections.IdentityHashMap;
import com.koch.ambeth.util.exception.RuntimeExceptionUtil;
import com.koch.ambeth.util.typeinfo.IPropertyInfo;
import com.koch.ambeth.util.typeinfo.IPropertyInfoProvider;

/**
 * Reference implementation for the <code>IObjectCopier</code> interface. Provides an extension point to customize to copy behavior on specific object types.
 * 
 */
public class ObjectCopier implements IObjectCopier, IObjectCopierExtendable, IThreadLocalCleanupBean
{
	@Autowired
	protected IPropertyInfoProvider propertyInfoProvider;

	/**
	 * Save an instance of ObjectCopierState per-thread for performance reasons
	 */
	protected final ThreadLocal<ObjectCopierState> ocStateTL = new ThreadLocal<ObjectCopierState>();

	// / <summary>
	// / Saves the current instance of ocStateTL to recognize recursive calls to the same ObjectCopier
	// / </summary>
	protected final ThreadLocal<ObjectCopierState> usedOcStateTL = new ThreadLocal<ObjectCopierState>();

	protected final ClassExtendableContainer<IObjectCopierExtension> extensions = new ClassExtendableContainer<IObjectCopierExtension>("objectCopierExtension",
			"type");

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void cleanupThreadLocal()
	{
		// Cleanup the TL variables. This is to be safe against memory leaks in thread pooling architectures
		ocStateTL.remove();
		usedOcStateTL.remove();
	}

	protected ObjectCopierState acquireObjectCopierState()
	{
		// Creates automatically a valid instance if this thread does not already have one
		ObjectCopierState ocState = ocStateTL.get();
		if (ocState == null)
		{
			ocState = new ObjectCopierState(this);
			ocStateTL.set(ocState);
		}
		return ocState;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public <T> T clone(T source)
	{
		// Don't clone a null object or immutable objects. Return the identical reference in these cases
		if (source == null || ImmutableTypeSet.isImmutableType(source.getClass()))
		{
			return source;
		}
		// Try to access current "in-use" ObjectCopierState first
		ObjectCopierState ocState = usedOcStateTL.get();
		if (ocState != null)
		{
			// Reuse TL instance. And do not bother with cleanup
			return cloneRecursive(source, ocState);
		}
		// No ObjectCopierState "in-use". So we set the TL instance "in-use" and clean it up in the end
		// because we are responsible for this in this case
		ocState = acquireObjectCopierState();
		usedOcStateTL.set(ocState);
		try
		{
			return cloneRecursive(source, ocState);
		}
		finally
		{
			// Clear "in-use" instance
			usedOcStateTL.remove();
			// Cleanup ObjectCopierState to allow reusage in the same thread later
			ocState.clear();
		}
	}

	/**
	 * Gets called by the ObjectCopierState on custom / default behavior switches
	 */
	@SuppressWarnings("unchecked")
	protected <T> T cloneRecursive(T source, ObjectCopierState ocState)
	{
		// Don't clone a null object or immutable objects. Return the identical reference in these cases
		if (source == null || ImmutableTypeSet.isImmutableType(source.getClass()))
		{
			return source;
		}
		Class<?> objType = source.getClass();
		IdentityHashMap<Object, Object> objectToCloneDict = ocState.objectToCloneDict;
		Object clone = objectToCloneDict.get(source);

		if (clone != null)
		{
			// Object has already been cloned. Cycle detected - we are finished here
			return (T) clone;
		}
		if (objType.isArray())
		{
			return (T) cloneArray(source, ocState);
		}
		if (source instanceof Collection)
		{
			return (T) cloneCollection(source, ocState);
		}
		// Check whether the object will be copied by custom behavior
		IObjectCopierExtension extension = extensions.getExtension(objType);
		if (extension != null)
		{
			clone = extension.deepClone(source, ocState);
			objectToCloneDict.put(source, clone);
			return (T) clone;
		}
		// Copy by default behavior
		return (T) cloneDefault(source, ocState);
	}

	protected Object cloneArray(Object source, ObjectCopierState ocState)
	{
		Class<?> objType = source.getClass();
		Class<?> elementType = objType.getComponentType();
		int length = Array.getLength(source);
		Object cloneArray = Array.newInstance(elementType, length);
		ocState.objectToCloneDict.put(source, cloneArray);
		if (ImmutableTypeSet.isImmutableType(elementType))
		{
			// Clone native array with native functionality for performance reasons
			System.arraycopy(source, 0, cloneArray, 0, length);
		}
		else
		{
			for (int a = length; a-- > 0;)
			{
				// Clone each item of the array
				Array.set(cloneArray, a, cloneRecursive(Array.get(source, a), ocState));
			}
		}
		return cloneArray;
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	protected Object cloneCollection(Object source, ObjectCopierState ocState)
	{
		Class<?> objType = source.getClass();
		Collection cloneColl;
		try
		{
			cloneColl = (Collection) objType.newInstance();
		}
		catch (Throwable e)
		{
			throw RuntimeExceptionUtil.mask(e);
		}
		ocState.objectToCloneDict.put(source, cloneColl);

		for (Object item : (Collection) source)
		{
			// Clone each item of the Collection
			Object cloneItem = cloneRecursive(item, ocState);
			cloneColl.add(cloneItem);
		}
		return cloneColl;
	}

	protected Object cloneDefault(Object source, ObjectCopierState ocState)
	{
		Class<?> objType = source.getClass();
		Object clone;
		try
		{
			clone = objType.newInstance();
		}
		catch (Throwable e)
		{
			throw RuntimeExceptionUtil.mask(e);
		}
		ocState.objectToCloneDict.put(source, clone);
		deepCloneProperties(source, clone, ocState);
		return clone;
	}

	protected void deepCloneProperties(Object source, Object clone, ObjectCopierState ocState)
	{
		IPropertyInfo[] properties = propertyInfoProvider.getPrivateProperties(source.getClass());
		for (IPropertyInfo property : properties)
		{
			if (!property.isWritable())
			{
				continue;
			}
			Object objValue = property.getValue(source);
			Object cloneValue = cloneRecursive(objValue, ocState);
			property.setValue(clone, cloneValue);
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void registerObjectCopierExtension(IObjectCopierExtension objectCopierExtension, Class<?> type)
	{
		// Delegate pattern to register the extension in the internal extension manager
		extensions.register(objectCopierExtension, type);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void unregisterObjectCopierExtension(IObjectCopierExtension objectCopierExtension, Class<?> type)
	{
		// Delegate pattern to unregister the extension from the internal extension manager
		extensions.unregister(objectCopierExtension, type);
	}
}
