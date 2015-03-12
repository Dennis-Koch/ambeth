package de.osthus.esmeralda.handler;

import java.util.Collections;
import java.util.List;

import com.sun.source.tree.StatementTree;
import com.sun.source.tree.Tree.Kind;

import de.osthus.ambeth.ioc.annotation.Autowired;
import de.osthus.esmeralda.IClassInfoManager;
import de.osthus.esmeralda.IConversionContext;
import de.osthus.esmeralda.snippet.ISnippetManager;

public abstract class AbstractStatementHandler<T extends StatementTree> implements IStatementHandlerExtension<T>
{
	public static final String INTENDED_BLANK = "// intended blank";

	@Autowired
	protected IClassInfoManager classInfoManager;

	@Autowired
	protected IConversionContext context;

	@Autowired
	protected IASTHelper astHelper;

	@Autowired
	protected IStatementHandlerRegistry statementHandlerRegistry;

	private String language;

	public AbstractStatementHandler()
	{
		// Intended blank
	}

	protected AbstractStatementHandler(String language)
	{
		this.language = language;
	}

	public String getLanguage()
	{
		if (language != null)
		{
			return language;
		}
		else
		{
			return context.getLanguage();
		}
	}

	@Override
	public void handle(T tree)
	{
		handle(tree, true);
	}

	protected void handleChildStatement(StatementTree statement)
	{
		handleChildStatement(statement, true);
	}

	protected void handleChildStatement(StatementTree statement, boolean standalone)
	{
		IConversionContext context = this.context.getCurrent();
		ISnippetManager snippetManager = context.getSnippetManager();

		Kind kind = statement.getKind();
		IStatementHandlerExtension<StatementTree> stmtHandler = statementHandlerRegistry.getExtension(getLanguage() + kind);

		if (stmtHandler != null && stmtHandler.getClass().getSimpleName().endsWith("BlockHandler"))
		{
			stmtHandler.handle(statement, standalone);
		}
		else if (stmtHandler != null)
		{
			context.incrementIndentationLevel();
			try
			{
				stmtHandler.handle(statement, standalone);
			}
			finally
			{
				context.decrementIndentationLevel();
			}
		}
		else if (standalone)
		{
			String statementString = statement.toString();
			List<String> untranslatableStatements = Collections.singletonList(statementString);
			snippetManager.writeSnippet(untranslatableStatements);
		}
		else
		{
			throw new IllegalArgumentException("Cannot handle embedded statement " + statement.toString());
		}
	}
}