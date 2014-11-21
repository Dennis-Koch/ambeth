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
					IStatementHandlerExtension<StatementTree> stmtHandler = statementHandlerRegistry.get(Lang.C_SHARP + kind);
					if (stmtHandler != null)
					{
						// Important to check here to keep the code in order
						checkUntranslatableList(untranslatableStatements, snippetManager);
						try
						{
							stmtHandler.handle(statement);
							continue;
						}
						catch (TypeResolveException e)
						{
							// Intentionally blank
							// Conversion of this statement failed. Let the SnippetManager handle it.

							// TODO think: To "rollback" already written code we should use an other writer in the handle() call. Or we use a writter that can
							// be resetted to save points.
							// A second writer would be better. That way we could do the untranslatable list check after we know we can handle the statement,
							// but put the snippet code before the statement code.
							// @DeK lass uns da Montag mal drüber reden. Das Exception-Handling ergibt hier so sehr viel Sinn, aber momentan wird so noch
							// kaputter Code produziert (siehe TestModule.cs).
						}
					}

					log.info("unhandled: " + kind + ": " + statement.getClass().getSimpleName() + ": " + statement.toString());
					String untranslatableStatement = statement.toString();
					untranslatableStatement = untranslatableStatement.endsWith(";") ? untranslatableStatement : untranslatableStatement + ";";
					untranslatableStatements.add(untranslatableStatement);
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
