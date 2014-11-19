package de.osthus.esmeralda.snippet;

import de.osthus.esmeralda.ConversionContext;
import de.osthus.esmeralda.ILanguageHelper;

public interface ISnippetManagerFactory
{
	ISnippetManager createSnippetManager(Object methodAstNode, ConversionContext context, ILanguageHelper languageHelper);
}
