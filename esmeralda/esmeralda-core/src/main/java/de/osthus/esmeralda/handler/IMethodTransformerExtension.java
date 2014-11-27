package de.osthus.esmeralda.handler;

import de.osthus.esmeralda.handler.csharp.MethodKey;

public interface IMethodTransformerExtension
{
	ITransformedMethod buildMethodTransformation(MethodKey methodKey);
}
