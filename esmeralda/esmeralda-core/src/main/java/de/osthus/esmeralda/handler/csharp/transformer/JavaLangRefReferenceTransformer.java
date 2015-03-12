package de.osthus.esmeralda.handler.csharp.transformer;

import de.osthus.ambeth.log.ILogger;
import de.osthus.ambeth.log.LogInstance;
import de.osthus.esmeralda.handler.uni.transformer.AbstractMethodTransformerExtension;

public class JavaLangRefReferenceTransformer extends AbstractMethodTransformerExtension
{
	@SuppressWarnings("unused")
	@LogInstance
	private ILogger log;

	@Override
	public void afterPropertiesSet() throws Throwable
	{
		super.afterPropertiesSet();

		mapTransformation(java.lang.ref.Reference.class, "get", "System.WeakReference", "Target", true);
	}
}
