package de.osthus.esmeralda.handler;

public interface IFieldTransformerExtensionExtendable
{
	void registerFieldTransformerExtension(IFieldTransformerExtension fieldTransformerExtension, String langPlusFqClassType);

	void unregisterFieldTransformerExtension(IFieldTransformerExtension fieldTransformerExtension, String langPlusFqClassType);
}
