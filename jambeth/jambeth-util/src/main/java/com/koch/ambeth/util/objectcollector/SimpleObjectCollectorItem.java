package com.koch.ambeth.util.objectcollector;

/*-
 * #%L
 * jambeth-util
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

import com.koch.ambeth.util.collections.ArrayList;
import com.koch.ambeth.util.exception.RuntimeExceptionUtil;

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
