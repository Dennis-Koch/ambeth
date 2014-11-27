package de.osthus.esmeralda.handler.csharp.transformer;

import de.osthus.ambeth.log.ILogger;
import de.osthus.ambeth.log.LogInstance;
import de.osthus.ambeth.util.StringConversionHelper;
import de.osthus.esmeralda.handler.ITransformedMethod;
import de.osthus.esmeralda.handler.TransformedMethod;
import de.osthus.esmeralda.handler.csharp.MethodKey;

public class DefaultMethodTransformer extends AbstractMethodTransformerExtension
{
	@SuppressWarnings("unused")
	@LogInstance
	private ILogger log;

	@Override
	protected ITransformedMethod buildMethodTransformationIntern(MethodKey methodKey)
	{
		String formattedMethodName = StringConversionHelper.upperCaseFirst(objectCollector, methodKey.getMethodName());
		TransformedMethod transformedMethod = new TransformedMethod(methodKey.getDeclaringTypeName(), formattedMethodName, methodKey.getParameters(), false,
				false);
		transformedMethod.setParameterProcessor(defaultMethodParameterProcessor);
		return transformedMethod;
	}
}
