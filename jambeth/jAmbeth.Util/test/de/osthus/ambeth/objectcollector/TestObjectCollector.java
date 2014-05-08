package de.osthus.ambeth.objectcollector;

import java.util.HashSet;
import java.util.Set;

public class TestObjectCollector extends NoOpObjectCollector
{
	protected Set<Object> collectables = new HashSet<Object>();

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.osthus.ambeth.objectcollector.NoOpObjectCollector#create(java.lang.Class)
	 */
	@Override
	public <T> T create(final Class<T> myClass)
	{
		T instance = super.create(myClass);

		if (instance instanceof ICollectable)
		{
			this.collectables.add(instance);
		}

		return instance;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.osthus.ambeth.objectcollector.NoOpObjectCollector#dispose(java.lang.Object)
	 */
	@Override
	public void dispose(final Object object)
	{
		if (object instanceof ICollectable)
		{
			this.collectables.remove(object);
		}
	}

	/**
	 * For debugging purposes only! Helps tracking undisposed objects.
	 * 
	 * @return Set of undisposed collectable objects.
	 */
	public Set<Object> getCollectablesInternDoNotCall()
	{
		return this.collectables;
	}
}
