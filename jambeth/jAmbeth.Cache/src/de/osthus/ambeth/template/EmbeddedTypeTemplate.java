package de.osthus.ambeth.template;

import de.osthus.ambeth.log.ILogger;
import de.osthus.ambeth.log.LogInstance;
import de.osthus.ambeth.model.IEmbeddedType;

public class EmbeddedTypeTemplate
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
