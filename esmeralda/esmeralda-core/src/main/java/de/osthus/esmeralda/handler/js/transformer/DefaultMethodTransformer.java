package de.osthus.esmeralda.handler.js.transformer;

import de.osthus.ambeth.log.ILogger;
import de.osthus.ambeth.log.LogInstance;
import de.osthus.esmeralda.handler.ITransformedMethod;
import de.osthus.esmeralda.handler.MethodKey;
import de.osthus.esmeralda.handler.TransformedMethod;
import de.osthus.esmeralda.handler.uni.transformer.AbstractMethodTransformerExtension;

public class DefaultMethodTransformer extends AbstractMethodTransformerExtension
{
	public static final String THIS = "constructor";

	public static final String SUPER = "superclass.constructor";

	@SuppressWarnings("unused")
	@LogInstance
	private ILogger log;

	@Override
	protected ITransformedMethod buildMethodTransformationIntern(MethodKey methodKey)
	{
		String methodName = methodKey.getMethodName();
		if ("super".equals(methodName))
		{
			methodName = SUPER;
		}
		else if ("this".equals(methodName))
		{
			methodName = THIS;
		}
		TransformedMethod transformedMethod = new TransformedMethod(methodKey.getDeclaringTypeName(), methodName, methodKey.getParameters(), false, false);
		transformedMethod.setParameterProcessor(defaultMethodParameterProcessor);
		return transformedMethod;
	}
}
