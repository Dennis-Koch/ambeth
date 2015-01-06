package de.osthus.esmeralda.handler.csharp.transformer;

import de.osthus.ambeth.log.ILogger;
import de.osthus.ambeth.log.LogInstance;
import de.osthus.esmeralda.handler.uni.transformer.AbstractMethodTransformerExtension;

public class JavaLangReflectFieldTransformer extends AbstractMethodTransformerExtension
{
	@SuppressWarnings("unused")
	@LogInstance
	private ILogger log;

	@Override
	public void afterPropertiesSet() throws Throwable
	{
		super.afterPropertiesSet();

		mapTransformation(java.lang.reflect.Field.class, "getName", "System.Reflection.FieldInfo", "Name", true);
		mapTransformation(java.lang.reflect.Field.class, "getType", "System.Reflection.FieldInfo", "FieldType", true);
	}
}
