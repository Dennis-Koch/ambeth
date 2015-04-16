package de.osthus.esmeralda.handler;

public interface IFieldTransformerExtensionRegistry
{
	IFieldTransformerExtension getExtension(String langPlusFqClassType);
}
