package de.osthus.esmeralda.handler;

public interface IMethodTransformerExtensionRegistry
{
	IMethodTransformerExtension getExtension(String langPlusFqClassType);
}
