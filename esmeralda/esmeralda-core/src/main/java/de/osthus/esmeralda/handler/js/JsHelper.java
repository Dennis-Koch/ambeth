package de.osthus.esmeralda.handler.js;

import java.io.IOException;
import java.io.Writer;

import de.osthus.ambeth.log.ILogger;
import de.osthus.ambeth.log.LogInstance;
import de.osthus.ambeth.threading.IBackgroundWorkerDelegate;
import de.osthus.esmeralda.ConversionContext;

public class JsHelper implements IJsHelper
{
	@SuppressWarnings("unused")
	@LogInstance
	private ILogger log;

	@Override
	public Writer newLineIntend(ConversionContext context, Writer writer) throws IOException
	{
		return null;
	}

	@Override
	public void scopeIntend(ConversionContext context, Writer writer, IBackgroundWorkerDelegate run) throws Throwable
	{
	}

	@Override
	public String camelCaseName(String typeName)
	{
		return null;
	}

	@Override
	public Writer writeType(String typeName, ConversionContext context, Writer writer) throws Throwable
	{
		return null;
	}
}
