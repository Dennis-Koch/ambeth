package de.osthus.esmeralda.handler.csharp.transformer;

import de.osthus.ambeth.log.ILogger;
import de.osthus.ambeth.log.LogInstance;
import de.osthus.esmeralda.IConversionContext;
import de.osthus.esmeralda.handler.ITransformedField;
import de.osthus.esmeralda.handler.TransformedMemberAccess;
import demo.codeanalyzer.common.model.Field;
import demo.codeanalyzer.common.model.JavaClassInfo;

public class DefaultFieldTransformer extends de.osthus.esmeralda.handler.uni.transformer.DefaultFieldTransformer
{
	@SuppressWarnings("unused")
	@LogInstance
	private ILogger log;

	@Override
	protected ITransformedField buildFieldTransformation(String owner, String name, IConversionContext context)
	{
		JavaClassInfo classInfo = classInfoManager.resolveClassInfo(owner);
		Field field = classInfo.getField(name);
		if (classInfo.isArray() && field.getName().equals("length"))
		{
			name = "Length";
		}
		return new TransformedMemberAccess(owner, name, field.getFieldType());
	}
}
