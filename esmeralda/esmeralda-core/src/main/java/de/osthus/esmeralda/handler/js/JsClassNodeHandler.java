package de.osthus.esmeralda.handler.js;

import java.io.Writer;

import de.osthus.ambeth.log.ILogger;
import de.osthus.ambeth.log.LogInstance;
import de.osthus.esmeralda.ConversionContext;
import de.osthus.esmeralda.handler.INodeHandlerExtension;

public class JsClassNodeHandler implements INodeHandlerExtension
{
	@SuppressWarnings("unused")
	@LogInstance
	private ILogger log;

	@Override
	public void handle(Object astNode, ConversionContext context, Writer writer)
	{
	}
}
