package de.osthus.esmeralda.handler.csharp.transformer;

import de.osthus.ambeth.log.ILogger;
import de.osthus.ambeth.log.LogInstance;
import de.osthus.esmeralda.handler.uni.transformer.AbstractMethodTransformerExtension;

public class JavaUtilIteratorTransformer extends AbstractMethodTransformerExtension
{
	@SuppressWarnings("unused")
	@LogInstance
	private ILogger log;

	@Override
	public void afterPropertiesSet() throws Throwable
	{
		super.afterPropertiesSet();

		mapTransformation(java.util.Iterator.class, "hasNext", "System.Collections.Generic.IEnumerator", "MoveNext", false);
		mapTransformation(java.util.Iterator.class, "next", "System.Collections.Generic.IEnumerator", "Current", true);
	}
}
