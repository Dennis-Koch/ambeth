package de.osthus.esmeralda.handler.js.stmt;

import com.sun.source.tree.BlockTree;

import de.osthus.ambeth.log.ILogger;
import de.osthus.ambeth.log.LogInstance;
import de.osthus.ambeth.threading.IBackgroundWorkerDelegate;
import de.osthus.esmeralda.IConversionContext;
import de.osthus.esmeralda.ILanguageHelper;
import de.osthus.esmeralda.handler.csharp.stmt.CsBlockHandler;
import de.osthus.esmeralda.misc.Lang;

public class JsBlockHandler extends CsBlockHandler
{
	@SuppressWarnings("unused")
	@LogInstance
	private ILogger log;

	public JsBlockHandler()
	{
		// Overwrite CS from super constructor
		language = Lang.JS;
	}

	@Override
	public void handle(final BlockTree blockTree, boolean standalone)
	{
		IConversionContext context = this.context.getCurrent();
		ILanguageHelper languageHelper = context.getLanguageHelper();

		languageHelper.scopeIntend(new IBackgroundWorkerDelegate()
		{
			@Override
			public void invoke() throws Throwable
			{
				writeBlockContentWithoutIntendation(blockTree);
			}
		});
	}
}
