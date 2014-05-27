package de.osthus.ambeth.objectcollector;

import de.osthus.ambeth.collections.ArrayList;
import de.osthus.ambeth.exception.RuntimeExceptionUtil;

public class ObjectCollectorItem implements IObjectCollectorItem
{
	protected final ArrayList<ICollectable> unusedList = new ArrayList<ICollectable>();

	protected int lowestSizeSinceLastCheck = 0;

	protected final Class<? extends ICollectable> constructorClass;

	protected final IObjectCollector objectCollector;

	protected final boolean collectorAware;

	public ObjectCollectorItem(IObjectCollector objectCollector, Class<? extends ICollectable> constructorClass)
	{
		this.objectCollector = objectCollector;
		this.constructorClass = constructorClass;
		collectorAware = IObjectCollectorAware.class.isAssignableFrom(constructorClass);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.osthus.ambeth.objectcollector.IObjectCollectorItem#getOneInstance()
	 */
	@Override
	public Object getOneInstance()
	{
		ICollectable elem = popLastElement();
		int listSize = unusedList.size();
		if (elem != null)
		{
			if (lowestSizeSinceLastCheck > listSize)
			{
				lowestSizeSinceLastCheck = listSize;
			}
		}
		else
		{
			try
			{
				elem = constructorClass.newInstance();
			}
			catch (Throwable e)
			{
				throw RuntimeExceptionUtil.mask(e);
			}
		}
		if (collectorAware)
		{
			((IObjectCollectorAware) elem).setObjectCollector(objectCollector);
		}
		elem.initInternDoNotCall();
		return elem;

	}

	protected ICollectable popLastElement()
	{
		return unusedList.popLastElement();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.osthus.ambeth.objectcollector.IObjectCollectorItem#dispose(java.lang .Object)
	 */
	@Override
	public void dispose(final Object object)
	{
		ICollectable obj = (ICollectable) object;
		obj.disposeInternDoNotCall();
		if (collectorAware)
		{
			((IObjectCollectorAware) object).setObjectCollector(null);
		}
		unusedList.add(obj);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.osthus.ambeth.objectcollector.IObjectCollectorItem#cleanUp()
	 */
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
