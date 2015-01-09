package de.osthus.ambeth.objectcollector;

import de.osthus.ambeth.appendable.AppendableStringBuilder;

public class AppendableStringBuilderCollectableController implements ICollectableController
{
	@Override
	public Object createInstance()
	{
		return new AppendableStringBuilder(new StringBuilder());
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
			((AppendableStringBuilder) object).reset();
		}
	}
}
