package de.osthus.esmeralda.handler.csharp.transformer;

import de.osthus.ambeth.log.ILogger;
import de.osthus.ambeth.log.LogInstance;
import de.osthus.ambeth.util.StringConversionHelper;
import de.osthus.esmeralda.handler.ITransformedMethod;
import de.osthus.esmeralda.handler.MethodKey;
import de.osthus.esmeralda.handler.TransformedMethod;
import de.osthus.esmeralda.handler.uni.transformer.AbstractMethodTransformerExtension;

public class DefaultMethodTransformer extends AbstractMethodTransformerExtension
{
	@SuppressWarnings("unused")
	@LogInstance
	private ILogger log;

	@Override
	protected ITransformedMethod buildMethodTransformationIntern(MethodKey methodKey)
	{
		String methodName = methodKey.getMethodName();
		if ("super".equals(methodName))
		{
			methodName = "base";
		}
		else if ("this".equals(methodName))
		{
			methodName = "this";
		}
		else
		{
			methodName = StringConversionHelper.upperCaseFirst(objectCollector, methodName);
		}
		TransformedMethod transformedMethod = new TransformedMethod(methodKey.getDeclaringTypeName(), methodName, methodKey.getParameters(), false);
		transformedMethod.setParameterProcessor(defaultMethodParameterProcessor);
		return transformedMethod;
	}
}
