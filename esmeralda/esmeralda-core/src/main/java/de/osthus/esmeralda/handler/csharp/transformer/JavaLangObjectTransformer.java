package de.osthus.esmeralda.handler.csharp.transformer;

import de.osthus.ambeth.log.ILogger;
import de.osthus.ambeth.log.LogInstance;

public class JavaLangObjectTransformer extends AbstractMethodTransformerExtension
{
	@SuppressWarnings("unused")
	@LogInstance
	private ILogger log;

	@Override
	public void afterPropertiesSet() throws Throwable
	{
		super.afterPropertiesSet();

		mapTransformation(java.lang.Object.class, "hashCode", "System.Object", "GetHashCode", false);
		mapTransformation(java.lang.Object.class, "getClass", "System.Object", "GetType", false);
	}
}
