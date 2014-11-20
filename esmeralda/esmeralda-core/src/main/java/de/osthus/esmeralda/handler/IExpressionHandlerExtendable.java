package de.osthus.esmeralda.handler;

public interface IExpressionHandlerExtendable
{
	void register(IExpressionHandler expressionHandler, Class<?> expressionType);

	void unregister(IExpressionHandler expressionHandler, Class<?> expressionType);
}
