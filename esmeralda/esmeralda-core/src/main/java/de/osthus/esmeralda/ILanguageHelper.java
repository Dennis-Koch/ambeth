package de.osthus.esmeralda;

import java.io.IOException;
import java.io.Writer;

import de.osthus.ambeth.threading.IBackgroundWorkerDelegate;

public interface ILanguageHelper
{
	Writer newLineIntend(ConversionContext context, Writer writer) throws IOException;

	void scopeIntend(ConversionContext context, Writer writer, IBackgroundWorkerDelegate run) throws Throwable;

	String camelCaseName(String typeName);

	Writer writeType(String typeName, ConversionContext context, Writer writer) throws Throwable;
}
