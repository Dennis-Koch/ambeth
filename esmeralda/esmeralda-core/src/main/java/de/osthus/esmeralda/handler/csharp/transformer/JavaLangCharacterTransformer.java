package de.osthus.esmeralda.handler.csharp.transformer;

import de.osthus.ambeth.log.ILogger;
import de.osthus.ambeth.log.LogInstance;
import de.osthus.esmeralda.handler.uni.transformer.AbstractMethodTransformerExtension;

public class JavaLangCharacterTransformer extends AbstractMethodTransformerExtension
{
	@SuppressWarnings("unused")
	@LogInstance
	private ILogger log;

	@Override
	public void afterPropertiesSet() throws Throwable
	{
		super.afterPropertiesSet();

		mapTransformation(Character.class, "toLowerCase", "System.Char", "ToLowerInvariant", false, Boolean.TRUE, true, char.class);
		mapTransformation(Character.class, "toUpperCase", "System.Char", "ToUpperInvariant", false, Boolean.TRUE, true, char.class);
	}
}
