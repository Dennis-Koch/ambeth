package de.osthus.esmeralda.handler.uni.stmt;

import java.lang.reflect.Field;
import java.util.List;

import com.sun.source.tree.BlockTree;
import com.sun.source.tree.StatementTree;
import com.sun.source.tree.Tree;
import com.sun.source.tree.Tree.Kind;

import de.osthus.ambeth.collections.ArrayList;
import de.osthus.ambeth.ioc.annotation.Autowired;
import de.osthus.ambeth.log.ILogger;
import de.osthus.ambeth.log.LogInstance;
import de.osthus.ambeth.threading.IBackgroundWorkerDelegate;
import de.osthus.ambeth.util.IConversionHelper;
import de.osthus.esmeralda.IConversionContext;
import de.osthus.esmeralda.ILanguageHelper;
import de.osthus.esmeralda.TypeResolveException;
import de.osthus.esmeralda.handler.AbstractStatementHandler;
import de.osthus.esmeralda.handler.IStatementHandlerExtension;
import de.osthus.esmeralda.misc.IToDoWriter;
import de.osthus.esmeralda.misc.StatementCount;
import de.osthus.esmeralda.snippet.ISnippetManager;
import de.osthus.esmeralda.snippet.SnippetTrigger;
import demo.codeanalyzer.common.model.Method;

public class UniversalBlockHandler extends AbstractStatementHandler<BlockTree> implements IStatementHandlerExtension<BlockTree>
{
	@SuppressWarnings("unused")
	@LogInstance
	private ILogger log;

	@Autowired
	protected IConversionHelper conversionHelper;

	@Autowired
	protected IToDoWriter todoWriter;

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
		Method method = context.getMethod();
		ISnippetManager snippetManager = context.getSnippetManager();
		StatementCount metric = context.getMetric();

		ArrayList<String> untranslatableStatements = new ArrayList<>();

		List<? extends StatementTree> statements = blockTree.getStatements();
		boolean dryRun = context.isDryRun();
		if (!dryRun)
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
					Tree previousTree = context.getCurrentTree();
					context.setCurrentTree(statement);
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
						checkUntranslatableList(untranslatableStatements, snippetManager, dryRun, context);

						context.getWriter().append(statementString);
					}
					catch (SnippetTrigger snippetTrigger)
					{
						String message = snippetTrigger.getMessage();
						int pos = findPos(statement);
						todoWriter.write(message, method, pos);
						if (log.isInfoEnabled() && !dryRun)
						{
							log.info(message);
						}
						addToUntranslatableList(untranslatableStatements, statement, dryRun, context, kind);
					}
					catch (TypeResolveException e)
					{
						if (log.isWarnEnabled() && !dryRun)
						{
							log.warn(e);
						}
						addToUntranslatableList(untranslatableStatements, statement, dryRun, context, kind);
					}
					finally
					{
						context.setCurrentTree(previousTree);
					}
				}
				else
				{
					addToUntranslatableList(untranslatableStatements, statement, dryRun, context, kind);
				}
			}
			checkUntranslatableList(untranslatableStatements, snippetManager, dryRun, context);
		}
		finally
		{
			context.setSkipFirstBlockStatement(skipFirstBlockStatement);
		}
	}

	protected int findPos(StatementTree statement)
	{
		try
		{
			Field field = statement.getClass().getField("pos");
			Object value = field.get(statement);
			int pos = conversionHelper.convertValueToType(Integer.class, value).intValue();
			return pos;
		}
		catch (NoSuchFieldException | SecurityException | IllegalArgumentException | IllegalAccessException e)
		{
			return -1;
		}
	}

	protected void addToUntranslatableList(ArrayList<String> untranslatableStatements, StatementTree statement, boolean dryRun, IConversionContext context,
			Kind kind)
	{
		if (dryRun)
		{
			return;
		}
		if (log.isInfoEnabled())
		{
			log.info(context.getClassInfo().getFqName() + ": unhandled - " + kind + ": " + statement.getClass().getSimpleName() + ": " + statement.toString());
		}

		String untranslatableStatement = statement.toString();
		untranslatableStatement = untranslatableStatement.endsWith(";") ? untranslatableStatement : untranslatableStatement + ";";
		untranslatableStatements.add(untranslatableStatement);
	}

	protected void checkUntranslatableList(ArrayList<String> untranslatableStatements, ISnippetManager snippetManager, boolean dryRun,
			IConversionContext context)
	{
		if (dryRun || untranslatableStatements.isEmpty())
		{
			return;
		}

		StatementCount metric = context.getMetric();

		metric.setUntranslatableStatements(metric.getUntranslatableStatements() + untranslatableStatements.size());

		snippetManager.writeSnippet(untranslatableStatements);
		untranslatableStatements.clear();
	}
}
