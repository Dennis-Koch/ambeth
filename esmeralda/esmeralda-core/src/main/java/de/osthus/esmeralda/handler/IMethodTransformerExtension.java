package de.osthus.esmeralda.handler;


public interface IMethodTransformerExtension
{
	ITransformedMethod buildMethodTransformation(MethodKey methodKey);
}
