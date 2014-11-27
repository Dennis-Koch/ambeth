package de.osthus.esmeralda.handler.csharp.stmt;

import java.util.Collections;
import java.util.List;

import com.sun.source.tree.StatementTree;
import com.sun.source.tree.Tree.Kind;

import de.osthus.ambeth.ioc.annotation.Autowired;
import de.osthus.esmeralda.IConversionContext;
import de.osthus.esmeralda.handler.IASTHelper;
import de.osthus.esmeralda.handler.IStatementHandlerExtension;
import de.osthus.esmeralda.handler.IStatementHandlerRegistry;
import de.osthus.esmeralda.handler.csharp.ICsHelper;
import de.osthus.esmeralda.misc.Lang;
import de.osthus.esmeralda.snippet.ISnippetManager;

public abstract class AbstractStatementHandler<T extends StatementTree> implements IStatementHandlerExtension<T>
{
	public static final String INTENDED_BLANK = "// intended blank";

	@Autowired
	protected IConversionContext context;

	@Autowired
	protected IASTHelper astHelper;

	@Autowired
	protected ICsHelper languageHelper;

	@Autowired
	protected IStatementHandlerRegistry statementHandlerRegistry;

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
		IStatementHandlerExtension<StatementTree> stmtHandler = statementHandlerRegistry.get(Lang.C_SHARP + kind);
		if (stmtHandler != null && stmtHandler.getClass().equals(CsBlockHandler.class))
		{
			stmtHandler.handle(statement, standalone);
		}
		else if (stmtHandler != null)
		{
			context.incremetIndentationLevel();
			stmtHandler.handle(statement, standalone);
			context.decremetIndentationLevel();
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