package de.osthus.esmeralda.handler;

import java.io.Writer;

import de.osthus.esmeralda.ConversionContext;

public interface INodeHandlerExtension
{
	void handle(Object astNode, ConversionContext context, Writer writer) throws Throwable;
}
