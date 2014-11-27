package de.osthus.esmeralda.handler;


public interface IMethodTransformerExtensionExtendable
{
	void registerMethodTransformerExtension(IMethodTransformerExtension methodTransformerExtension, String fqClassType);

	void unregisterMethodTransformerExtension(IMethodTransformerExtension methodTransformerExtension, String fqClassType);
}
