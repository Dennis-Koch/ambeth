package de.osthus.esmeralda.handler.csharp.stmt;

import java.util.List;

import com.sun.source.tree.BlockTree;
import com.sun.source.tree.StatementTree;
import com.sun.source.tree.Tree.Kind;

import de.osthus.ambeth.collections.ArrayList;
import de.osthus.ambeth.log.ILogger;
import de.osthus.ambeth.log.LogInstance;
import de.osthus.ambeth.threading.IBackgroundWorkerDelegate;
import de.osthus.esmeralda.IConversionContext;
import de.osthus.esmeralda.TypeResolveException;
import de.osthus.esmeralda.handler.IStatementHandlerExtension;
import de.osthus.esmeralda.misc.Lang;
import de.osthus.esmeralda.misc.StatementCount;
import de.osthus.esmeralda.snippet.ISnippetManager;

public class CsBlockHandler extends AbstractStatementHandler<BlockTree> implements IStatementHandlerExtension<BlockTree>, ICsBlockHandler
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
				writeBlockContentWithoutIntendation(blockTree);
			}
		});
	}

	@Override
	public void writeBlockContentWithoutIntendation(BlockTree blockTree)
	{
		IConversionContext context = CsBlockHandler.this.context.getCurrent();
		ISnippetManager snippetManager = context.getSnippetManager();
		StatementCount metric = context.getMetric();

		ArrayList<String> untranslatableStatements = new ArrayList<>();

		List<? extends StatementTree> statements = blockTree.getStatements();
		if (!context.isDryRun())
		{
			metric.setStatements(metric.getStatements() + statements.size());
		}
		for (StatementTree statement : statements)
		{
			Kind kind = statement.getKind();
			final IStatementHandlerExtension<StatementTree> stmtHandler = statementHandlerRegistry.get(Lang.C_SHARP + kind);
			if (stmtHandler != null)
			{
				// Important to check here to keep the code in order
				checkUntranslatableList(untranslatableStatements, snippetManager);

				final StatementTree fstatement = statement;
				try
				{
					String statementString = languageHelper.writeToStash(new IBackgroundWorkerDelegate()
					{
						@Override
						public void invoke() throws Throwable
						{
							stmtHandler.handle(fstatement);
						}
					});

					// Important to check here to keep the code in order
					checkUntranslatableList(untranslatableStatements, snippetManager);

					context.getWriter().append(statementString);
				}
				catch (TypeResolveException e)
				{
					log.info(context.getClassInfo().getFqName() + ": unhandled - " + kind + ": " + statement.getClass().getSimpleName() + ": "
							+ statement.toString());

					String untranslatableStatement = statement.toString();
					untranslatableStatement = untranslatableStatement.endsWith(";") ? untranslatableStatement : untranslatableStatement + ";";
					untranslatableStatements.add(untranslatableStatement);
				}
			}
		}
		checkUntranslatableList(untranslatableStatements, snippetManager);
	}

	protected void checkUntranslatableList(ArrayList<String> untranslatableStatements, ISnippetManager snippetManager)
	{
		if (untranslatableStatements.isEmpty())
		{
			return;
		}

		if (!context.isDryRun())
		{
			IConversionContext context = CsBlockHandler.this.context.getCurrent();
			StatementCount metric = context.getMetric();

			metric.setUntranslatableStatements(metric.getUntranslatableStatements() + untranslatableStatements.size());
		}

		snippetManager.writeSnippet(untranslatableStatements);
		untranslatableStatements.clear();
	}
}
