package de.osthus.esmeralda.handler.csharp.stmt;

import java.util.Iterator;
import java.util.List;

import com.sun.source.tree.BlockTree;
import com.sun.source.tree.StatementTree;
import com.sun.source.tree.Tree.Kind;

import de.osthus.ambeth.collections.ArrayList;
import de.osthus.ambeth.ioc.annotation.Autowired;
import de.osthus.ambeth.log.ILogger;
import de.osthus.ambeth.log.LogInstance;
import de.osthus.ambeth.threading.IBackgroundWorkerDelegate;
import de.osthus.esmeralda.IConversionContext;
import de.osthus.esmeralda.handler.IStatementHandlerExtension;
import de.osthus.esmeralda.handler.IStatementHandlerRegistry;
import de.osthus.esmeralda.handler.csharp.ICsharpHelper;
import de.osthus.esmeralda.misc.Lang;
import de.osthus.esmeralda.snippet.ISnippetManager;

public class CsBlockHandler implements IStatementHandlerExtension<BlockTree>
{
	@SuppressWarnings("unused")
	@LogInstance
	private ILogger log;

	@Autowired
	protected IConversionContext context;

	@Autowired
	protected ICsharpHelper languageHelper;

	@Autowired
	protected IStatementHandlerRegistry statementHandlerRegistry;

	@Override
	public void handle(final BlockTree blockTree)
	{
		languageHelper.scopeIntend(new IBackgroundWorkerDelegate()
		{
			@SuppressWarnings("unused")
			@Override
			public void invoke() throws Throwable
			{
				IConversionContext context = CsBlockHandler.this.context.getCurrent();
				ISnippetManager snippetManager = context.getSnippetManager();

				List<? extends StatementTree> statements = blockTree.getStatements();
				Iterator<? extends StatementTree> iter = statements.iterator();

				ArrayList<String> untranslatableStatements = new ArrayList<>();

				while (iter.hasNext())
				{
					StatementTree statement = iter.next();
					Kind kind = statement.getKind();
					IStatementHandlerExtension<StatementTree> stmtHandler = statementHandlerRegistry.get(Lang.C_SHARP + kind);
					if (stmtHandler != null)
					{
						checkUntranslatableList(untranslatableStatements, snippetManager);
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
