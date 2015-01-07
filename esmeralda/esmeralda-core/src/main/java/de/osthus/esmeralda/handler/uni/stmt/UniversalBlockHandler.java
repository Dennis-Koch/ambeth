package de.osthus.esmeralda.handler.uni.stmt;

import java.util.List;

import com.sun.source.tree.BlockTree;
import com.sun.source.tree.StatementTree;
import com.sun.source.tree.Tree.Kind;

import de.osthus.ambeth.collections.ArrayList;
import de.osthus.ambeth.log.ILogger;
import de.osthus.ambeth.log.LogInstance;
import de.osthus.ambeth.threading.IBackgroundWorkerDelegate;
import de.osthus.esmeralda.IConversionContext;
import de.osthus.esmeralda.ILanguageHelper;
import de.osthus.esmeralda.TypeResolveException;
import de.osthus.esmeralda.handler.AbstractStatementHandler;
import de.osthus.esmeralda.handler.IStatementHandlerExtension;
import de.osthus.esmeralda.misc.StatementCount;
import de.osthus.esmeralda.snippet.ISnippetManager;

public class UniversalBlockHandler extends AbstractStatementHandler<BlockTree> implements IStatementHandlerExtension<BlockTree>
{
	@SuppressWarnings("unused")
	@LogInstance
	private ILogger log;

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

	public void writeBlockContentWithoutIntendation(BlockTree blockTree)
	{
		IConversionContext context = UniversalBlockHandler.this.context.getCurrent();
		ISnippetManager snippetManager = context.getSnippetManager();
		StatementCount metric = context.getMetric();

		ArrayList<String> untranslatableStatements = new ArrayList<>();

		List<? extends StatementTree> statements = blockTree.getStatements();
		boolean noDryRun = !context.isDryRun();
		if (noDryRun)
		{
			metric.setStatements(metric.getStatements() + statements.size());
		}
		boolean skipFirstBlockStatement = context.isSkipFirstBlockStatement();
		context.setSkipFirstBlockStatement(false);
		try
		{
			for (int a = skipFirstBlockStatement ? 1 : 0, size = statements.size(); a < size; a++)
			{
				StatementTree statement = statements.get(a);
				Kind kind = statement.getKind();
				final IStatementHandlerExtension<StatementTree> stmtHandler = statementHandlerRegistry.getExtension(getLanguage() + kind);
				if (stmtHandler != null)
				{
					final StatementTree fstatement = statement;
					try
					{
						String statementString = astHelper.writeToStash(new IBackgroundWorkerDelegate()
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
						log.warn(e);
						addToUntranslatableList(untranslatableStatements, statement, noDryRun, context, kind);
					}
				}
				else
				{
					addToUntranslatableList(untranslatableStatements, statement, noDryRun, context, kind);
				}
			}
			checkUntranslatableList(untranslatableStatements, snippetManager);
		}
		finally
		{
			context.setSkipFirstBlockStatement(skipFirstBlockStatement);
		}
	}

	protected void addToUntranslatableList(ArrayList<String> untranslatableStatements, StatementTree statement, boolean noDryRun, IConversionContext context,
			Kind kind)
	{
		if (log.isInfoEnabled() && noDryRun)
		{
			log.info(context.getClassInfo().getFqName() + ": unhandled - " + kind + ": " + statement.getClass().getSimpleName() + ": " + statement.toString());
		}

		String untranslatableStatement = statement.toString();
		untranslatableStatement = untranslatableStatement.endsWith(";") ? untranslatableStatement : untranslatableStatement + ";";
		untranslatableStatements.add(untranslatableStatement);
	}

	protected void checkUntranslatableList(ArrayList<String> untranslatableStatements, ISnippetManager snippetManager)
	{
		if (untranslatableStatements.isEmpty())
		{
			return;
		}

		if (!context.isDryRun())
		{
			IConversionContext context = UniversalBlockHandler.this.context.getCurrent();
			StatementCount metric = context.getMetric();

			metric.setUntranslatableStatements(metric.getUntranslatableStatements() + untranslatableStatements.size());
		}

		snippetManager.writeSnippet(untranslatableStatements);
		untranslatableStatements.clear();
	}
}
