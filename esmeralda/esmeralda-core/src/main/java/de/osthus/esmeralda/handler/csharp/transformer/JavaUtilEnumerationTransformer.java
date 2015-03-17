package de.osthus.esmeralda.handler.csharp.transformer;

import de.osthus.ambeth.log.ILogger;
import de.osthus.ambeth.log.LogInstance;
import de.osthus.esmeralda.handler.uni.transformer.AbstractMethodTransformerExtension;

public class JavaUtilEnumerationTransformer extends AbstractMethodTransformerExtension
{
	@SuppressWarnings("unused")
	@LogInstance
	private ILogger log;

	@Override
	public void afterPropertiesSet() throws Throwable
	{
		super.afterPropertiesSet();

		mapTransformation(java.util.Enumeration.class, "hasMoreElements", "System.Collections.Generic.IEnumerator", "MoveNext", false);
		mapTransformation(java.util.Enumeration.class, "nextElement", "System.Collections.Generic.IEnumerator", "Current", true);
	}
}
