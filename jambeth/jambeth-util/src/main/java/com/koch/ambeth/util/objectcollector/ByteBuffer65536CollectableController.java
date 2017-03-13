package com.koch.ambeth.util.objectcollector;

import java.nio.ByteBuffer;

public class ByteBuffer65536CollectableController implements ICollectableController
{
	@Override
	public Object createInstance()
	{
		return ByteBuffer.allocate(65536);
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
