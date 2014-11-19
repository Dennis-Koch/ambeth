package de.osthus.esmeralda.snippet;

import de.osthus.ambeth.ioc.IServiceContext;
import de.osthus.ambeth.ioc.annotation.Autowired;
import de.osthus.ambeth.log.ILogger;
import de.osthus.ambeth.log.LogInstance;
import de.osthus.esmeralda.ConversionContext;
import de.osthus.esmeralda.ILanguageHelper;

public class SnippetManagerFactory implements ISnippetManagerFactory
{
	@SuppressWarnings("unused")
	@LogInstance
	private ILogger log;

	@Autowired
	protected IServiceContext serviceContext;

	@Override
	public ISnippetManager createSnippetManager(Object methodAstNode, ConversionContext context, ILanguageHelper languageHelper)
	{
		ISnippetManager snippetManager = serviceContext.registerBean(SnippetManager.class) //
				.propertyValue("languageHelper", languageHelper) //
				.propertyValue("methodAstNode", methodAstNode) //
				.propertyValue("context", context) //
				.finish();
		return snippetManager;
	}
}
