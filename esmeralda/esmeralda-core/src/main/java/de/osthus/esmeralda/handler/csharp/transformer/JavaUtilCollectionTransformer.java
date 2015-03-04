package de.osthus.esmeralda.handler.csharp.transformer;

import de.osthus.ambeth.log.ILogger;
import de.osthus.ambeth.log.LogInstance;
import de.osthus.esmeralda.handler.uni.transformer.AbstractMethodTransformerExtension;

public class JavaUtilCollectionTransformer extends AbstractMethodTransformerExtension
{
	@SuppressWarnings("unused")
	@LogInstance
	private ILogger log;

	@Override
	public void afterPropertiesSet() throws Throwable
	{
		super.afterPropertiesSet();

		mapTransformation(java.util.Collection.class, "size", "System.Collections.Generic.ICollection", "Count", true);
		mapTransformation(java.util.Collection.class, "iterator", "System.Collections.Generic.ICollection", "GetEnumerator", false);
	}
}
