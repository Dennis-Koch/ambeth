package de.osthus.esmeralda.snippet;

import de.osthus.esmeralda.ConversionContext;

public interface ISnippetManager
{
	String getSnippet(Object astNode, ConversionContext context);
}
