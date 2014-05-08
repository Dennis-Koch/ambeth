package de.osthus.ambeth.objectcollector;

import de.osthus.ambeth.exception.RuntimeExceptionUtil;

public class NoOpObjectCollector implements IObjectCollector, IThreadLocalObjectCollector, ICollectableControllerExtendable
{
	@Override
	public void registerCollectableController(ICollectableController collectableController, Class<?> handledType)
	{
		// Intended blank
	}

	@Override
	public void unregisterCollectableController(ICollectableController collectableController, Class<?> handledType)
	{
		// Intended blank
	}

	@Override
	public IThreadLocalObjectCollector getCurrent()
	{
		return this;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.osthus.ambeth.objectcollector.IObjectCollector#create(java.lang.Class)
	 */
	@Override
	public <T> T create(final Class<T> myClass)
	{
		try
		{
			T instance = myClass.newInstance();
			if (instance instanceof IObjectCollectorAware)
			{
				((IObjectCollectorAware) instance).setObjectCollector(this);
			}
			if (instance instanceof ICollectable)
			{
				((ICollectable) instance).initInternDoNotCall();
			}
			return instance;
		}
		catch (Throwable e)
		{
			throw RuntimeExceptionUtil.mask(e, "Error occured while instantiating type " + myClass);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.osthus.ambeth.objectcollector.IObjectCollector#dispose(de.osthus.ambeth .objectcollector.Collectable)
	 */
	@Override
	public void dispose(final Object object)
	{
		if (object instanceof ICollectable)
		{
			((ICollectable) object).disposeInternDoNotCall();
		}
		if (object instanceof IObjectCollectorAware)
		{
			((IObjectCollectorAware) object).setObjectCollector(null);
		}
	}

	@Override
	public <T> void dispose(Class<T> type, T object)
	{
		if (object instanceof ICollectable)
		{
			((ICollectable) object).disposeInternDoNotCall();
		}
		if (object instanceof IObjectCollectorAware)
		{
			((IObjectCollectorAware) object).setObjectCollector(null);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.osthus.ambeth.objectcollector.IObjectCollector#cleanUp()
	 */
	@Override
	public void cleanUp()
	{
		// Intended blank
	}
}
