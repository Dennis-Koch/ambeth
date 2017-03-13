package com.koch.ambeth.cache.mixin;

import com.koch.ambeth.log.ILogger;
import com.koch.ambeth.log.LogInstance;
import com.koch.ambeth.util.model.IEmbeddedType;

public class EmbeddedTypeMixin
{
	@SuppressWarnings("unused")
	@LogInstance
	private ILogger log;

	public final Object getRoot(IEmbeddedType embeddedObject)
	{
		Object parent = embeddedObject.getParent();
		while (parent instanceof IEmbeddedType)
		{
			parent = ((IEmbeddedType) parent).getParent();
		}
		return parent;
	}
}
