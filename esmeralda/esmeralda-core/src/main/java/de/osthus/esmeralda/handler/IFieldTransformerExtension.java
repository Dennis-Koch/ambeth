package de.osthus.esmeralda.handler;

public interface IFieldTransformerExtension
{
	ITransformedField buildFieldTransformation(String owner, String name);
}
