package de.osthus.ambeth.objectcollector;

import de.osthus.ambeth.collections.ArrayList;
import de.osthus.ambeth.exception.RuntimeExceptionUtil;

public class SimpleObjectCollectorItem implements IObjectCollectorItem
{
	protected final ArrayList<Object> unusedList = new ArrayList<Object>();

	protected int lowestSizeSinceLastCheck = 0;

	protected final Class<?> constructorClass;

	protected final IObjectCollector objectCollector;

	protected final boolean collectorAware;

	protected final ICollectableController collectableController;

	protected SimpleObjectCollectorItem(IObjectCollector objectCollector, ICollectableController collectableController, Class<?> constructorClass)
	{
		if (collectableController == null)
		{
			throw new IllegalArgumentException("No " + ICollectableController.class.getSimpleName() + " found for type " + constructorClass.getName());
		}
		this.objectCollector = objectCollector;
		this.collectableController = collectableController;
		this.constructorClass = constructorClass;
		collectorAware = IObjectCollectorAware.class.isAssignableFrom(constructorClass);
	}

	@Override
	public Object getOneInstance()
	{
		Object elem = popLastElement();
		int listSize = unusedList.size();
		try
		{
			if (elem != null)
			{
				if (lowestSizeSinceLastCheck > listSize)
				{
					lowestSizeSinceLastCheck = listSize;
				}
			}
			else
			{
				elem = collectableController.createInstance();
			}
			if (collectorAware)
			{
				((IObjectCollectorAware) elem).setObjectCollector(objectCollector);
			}
			collectableController.initObject(elem);
			return elem;
		}
		catch (Throwable e)
		{
			throw RuntimeExceptionUtil.mask(e);
		}
	}

	protected Object popLastElement()
	{
		return unusedList.popLastElement();
	}

	@Override
	public void dispose(final Object object)
	{
		try
		{
			collectableController.disposeObject(object);
		}
		catch (Throwable e)
		{
			throw RuntimeExceptionUtil.mask(e);
		}
		if (collectorAware)
		{
			((IObjectCollectorAware) object).setObjectCollector(null);
		}
		unusedList.add(object);
	}

	@Override
	public void cleanUp()
	{
		while (lowestSizeSinceLastCheck-- > 0)
		{
			unusedList.popLastElement();
		}
		lowestSizeSinceLastCheck = unusedList.size();
	}
}
