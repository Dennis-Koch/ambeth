package de.osthus.esmeralda.handler.js.stmt;

import com.sun.source.tree.BlockTree;
import com.sun.source.tree.StatementTree;
import com.sun.source.tree.Tree.Kind;

import de.osthus.ambeth.log.ILogger;
import de.osthus.ambeth.log.LogInstance;
import de.osthus.ambeth.threading.IBackgroundWorkerDelegate;
import de.osthus.esmeralda.handler.IStatementHandlerExtension;
import de.osthus.esmeralda.handler.csharp.stmt.CsBlockHandler;
import de.osthus.esmeralda.handler.js.IJsHelper;
import de.osthus.esmeralda.misc.Lang;

public class JsBlockHandler extends CsBlockHandler
{
	@SuppressWarnings("unused")
	@LogInstance
	private ILogger log;

	public JsBlockHandler()
	{
		language = Lang.JS;
	}

	public void setLanguageHelper(IJsHelper languageHelper)
	{
		this.languageHelper = languageHelper;
	}

	@Override
	public void handle(final BlockTree blockTree, boolean standalone)
	{
		languageHelper.scopeIntend(new IBackgroundWorkerDelegate()
		{
			@Override
			public void invoke() throws Throwable
			{
				writeBlockContentWithoutIntendation(blockTree);
			}
		});
	}

	@Override
	protected IStatementHandlerExtension<StatementTree> getStatementHandler(Kind kind)
	{
		IStatementHandlerExtension<StatementTree> stmtHandler = statementHandlerRegistry.getExtension(Lang.JS + kind);
		return stmtHandler;
	}
}
