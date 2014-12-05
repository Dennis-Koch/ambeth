package de.osthus.esmeralda.handler;

public interface IExpressionHandlerRegistry
{
	IExpressionHandler getExtension(String key);
}
