package de.osthus.esmeralda.handler.csharp.transformer;

import de.osthus.ambeth.log.ILogger;
import de.osthus.ambeth.log.LogInstance;
import de.osthus.esmeralda.handler.uni.transformer.AbstractMethodTransformerExtension;

public class JavaLangThreadLocalTransformer extends AbstractMethodTransformerExtension
{
	@SuppressWarnings("unused")
	@LogInstance
	private ILogger log;

	@Override
	public void afterPropertiesSet() throws Throwable
	{
		super.afterPropertiesSet();

		mapTransformation(java.lang.ThreadLocal.class, "get", "System.Threading.ThreadLocal", "Value", true);
		mapTransformation(java.lang.ThreadLocal.class, "set", "System.Threading.ThreadLocal", "Value", true, Object.class);
		mapTransformation(java.lang.ThreadLocal.class, "set", "System.Threading.ThreadLocal", "Value", true, (Class<?>) null);
	}
}
