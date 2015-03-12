package de.osthus.esmeralda.handler.csharp.transformer;

import de.osthus.ambeth.log.ILogger;
import de.osthus.ambeth.log.LogInstance;
import de.osthus.esmeralda.handler.uni.transformer.AbstractMethodTransformerExtension;

public class JavaUtilListTransformer extends AbstractMethodTransformerExtension
{
	@SuppressWarnings("unused")
	@LogInstance
	private ILogger log;

	@Override
	public void afterPropertiesSet() throws Throwable
	{
		super.afterPropertiesSet();

		mapTransformation(java.util.List.class, "size", "System.Collections.ICollection", "Count", true);
		mapTransformation(java.util.List.class, "get", "System.Collections.Generic.IList", "idx[]", false, int.class).setIndexedInvocation(true);
	}
}
