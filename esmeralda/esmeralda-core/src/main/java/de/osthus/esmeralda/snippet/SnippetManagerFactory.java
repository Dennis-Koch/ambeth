package de.osthus.esmeralda.snippet;

import de.osthus.ambeth.ioc.IServiceContext;
import de.osthus.ambeth.ioc.annotation.Autowired;
import de.osthus.ambeth.log.ILogger;
import de.osthus.ambeth.log.LogInstance;

public class SnippetManagerFactory implements ISnippetManagerFactory
{
	@SuppressWarnings("unused")
	@LogInstance
	private ILogger log;

	@Autowired
	protected IServiceContext serviceContext;

	@Override
	public ISnippetManager createSnippetManager()
	{
		ISnippetManager snippetManager = serviceContext.registerBean(SnippetManager.class).finish();
		return snippetManager;
	}
}
