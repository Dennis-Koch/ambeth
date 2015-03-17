package de.osthus.esmeralda.handler;

public interface IMethodTransformerExtensionExtendable
{
	void registerMethodTransformerExtension(IMethodTransformerExtension methodTransformerExtension, String langPlusFqClassType);

	void unregisterMethodTransformerExtension(IMethodTransformerExtension methodTransformerExtension, String langPlusFqClassType);
}
