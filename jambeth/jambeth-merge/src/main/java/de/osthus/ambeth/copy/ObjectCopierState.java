package de.osthus.ambeth.copy;

import de.osthus.ambeth.collections.IdentityHashMap;

/**
 * Encapsulates the internal state of an ObjectCopier operation
 */
public class ObjectCopierState implements IObjectCopierState
{
	protected final IdentityHashMap<Object, Object> objectToCloneDict = new IdentityHashMap<Object, Object>();

	protected final ObjectCopier objectCopier;

	public ObjectCopierState(ObjectCopier objectCopier)
	{
		this.objectCopier = objectCopier;
	}

	/**
	 * @inheritDoc
	 */
	@Override
	public <T> void addClone(T source, T clone)
	{
		if (!objectToCloneDict.putIfNotExists(source, clone))
		{
			throw new IllegalStateException("Object '" + source + "' has already been copied before in this recursive operation");
		}
	}

	/**
	 * @inheritDoc
	 */
	@Override
	public <T> T clone(T source)
	{
		return objectCopier.cloneRecursive(source, this);
	}

	/**
	 * Called to prepare this instance for clean reusage
	 */
	public void clear()
	{
		objectToCloneDict.clear();
	}
}
