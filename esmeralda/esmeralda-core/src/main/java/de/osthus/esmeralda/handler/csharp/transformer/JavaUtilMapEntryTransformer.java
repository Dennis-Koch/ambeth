package de.osthus.esmeralda.handler.csharp.transformer;

import de.osthus.ambeth.ioc.annotation.Autowired;
import de.osthus.ambeth.log.ILogger;
import de.osthus.ambeth.log.LogInstance;
import de.osthus.esmeralda.handler.js.IJsHelper;
import de.osthus.esmeralda.handler.uni.transformer.AbstractMethodTransformerExtension;

public class JavaUtilMapEntryTransformer extends AbstractMethodTransformerExtension
{
	@SuppressWarnings("unused")
	@LogInstance
	private ILogger log;

	@Autowired
	protected IJsHelper languageHelper;

	@Override
	public void afterPropertiesSet() throws Throwable
	{
		super.afterPropertiesSet();

		mapTransformation(java.util.Map.Entry.class, "getKey", "De.Osthus.Ambeth.Collections.Entry", "Key", true);
		mapTransformation(java.util.Map.Entry.class, "getValue", "De.Osthus.Ambeth.Collections.Entry", "Value", true);
	}
}
