package de.osthus.esmeralda.handler.csharp.stmt;

import com.sun.source.tree.BlockTree;
import com.sun.source.tree.StatementTree;
import com.sun.source.tree.Tree.Kind;

import de.osthus.ambeth.collections.ArrayList;
import de.osthus.ambeth.log.ILogger;
import de.osthus.ambeth.log.LogInstance;
import de.osthus.ambeth.threading.IBackgroundWorkerDelegate;
import de.osthus.esmeralda.IConversionContext;
import de.osthus.esmeralda.handler.IStatementHandlerExtension;
import de.osthus.esmeralda.misc.Lang;
import de.osthus.esmeralda.snippet.ISnippetManager;

public class CsBlockHandler extends AbstractStatementHandler<BlockTree> implements IStatementHandlerExtension<BlockTree>
{
	@SuppressWarnings("unused")
	@LogInstance
	private ILogger log;

	@Override
	public void handle(final BlockTree blockTree, boolean standalone)
	{
		languageHelper.scopeIntend(new IBackgroundWorkerDelegate()
		{
			@Override
			public void invoke() throws Throwable
			{
				IConversionContext context = CsBlockHandler.this.context.getCurrent();
				ISnippetManager snippetManager = context.getSnippetManager();

				ArrayList<String> untranslatableStatements = new ArrayList<>();

				for (StatementTree statement : blockTree.getStatements())
				{
					Kind kind = statement.getKind();
					IStatementHandlerExtension<StatementTree> stmtHandler = statementHandlerRegistry.get(Lang.C_SHARP + kind);
					if (stmtHandler != null)
					{
						stmtHandler.handle(statement);
					}
					else
					{
						log.info("unhandled: " + kind + ": " + statement.getClass().getSimpleName() + ": " + statement.toString());
						untranslatableStatements.add(statement.toString() + ";");
					}
				}
				checkUntranslatableList(untranslatableStatements, snippetManager);
			}
		});
	}

	protected void checkUntranslatableList(ArrayList<String> untranslatableStatements, ISnippetManager snippetManager)
	{
		if (untranslatableStatements.isEmpty())
		{
			return;
		}

		snippetManager.writeSnippet(untranslatableStatements);
		untranslatableStatements.clear();
	}
}
