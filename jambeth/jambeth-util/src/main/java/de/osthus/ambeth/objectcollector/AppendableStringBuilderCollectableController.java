package de.osthus.ambeth.objectcollector;

import java.nio.ByteBuffer;

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
			((ByteBuffer) object).clear();
		}
	}
}
