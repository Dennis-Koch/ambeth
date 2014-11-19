package de.osthus.esmeralda.snippet;

import com.sun.source.tree.MethodTree;

import de.osthus.esmeralda.ILanguageHelper;

public interface ISnippetManagerFactory
{
	ISnippetManager createSnippetManager(MethodTree methodTree, ILanguageHelper languageHelper);
}
