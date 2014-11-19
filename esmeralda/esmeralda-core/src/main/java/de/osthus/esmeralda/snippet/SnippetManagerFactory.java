package de.osthus.esmeralda.snippet;

import com.sun.source.tree.MethodTree;

import de.osthus.ambeth.ioc.IServiceContext;
import de.osthus.ambeth.ioc.annotation.Autowired;
import de.osthus.ambeth.log.ILogger;
import de.osthus.ambeth.log.LogInstance;
import de.osthus.esmeralda.ILanguageHelper;

public class SnippetManagerFactory implements ISnippetManagerFactory
{
	@SuppressWarnings("unused")
	@LogInstance
	private ILogger log;

	@Autowired
	protected IServiceContext serviceContext;

	@Override
	public ISnippetManager createSnippetManager(MethodTree methodTree, ILanguageHelper languageHelper)
	{
		ISnippetManager snippetManager = serviceContext.registerBean(SnippetManager.class) //
				.propertyValue("languageHelper", languageHelper) //
				.propertyValue("methodTree", methodTree) //
				.finish();
		return snippetManager;
	}
}
