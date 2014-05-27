package de.osthus.ambeth.util;

import de.osthus.ambeth.objectcollector.ICollectableController;

public class StringBuilderCollectableController implements ICollectableController
{
	@Override
	public Object createInstance()
	{
		return new StringBuilder();
	}

	@Override
	public void initObject(Object object)
	{
		// Intended blank
	}

	@Override
	public void disposeObject(Object object)
	{
		if (object != null)
		{
			((StringBuilder) object).setLength(0);
		}
	}
}
