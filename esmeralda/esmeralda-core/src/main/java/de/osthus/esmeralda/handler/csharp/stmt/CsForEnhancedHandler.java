package de.osthus.esmeralda.handler.csharp.stmt;

import java.util.Collections;
import java.util.List;

import com.sun.source.tree.StatementTree;
import com.sun.source.tree.Tree.Kind;
import com.sun.tools.javac.tree.JCTree.JCEnhancedForLoop;

import de.osthus.ambeth.ioc.annotation.Autowired;
import de.osthus.ambeth.log.ILogger;
import de.osthus.ambeth.log.LogInstance;
import de.osthus.esmeralda.IConversionContext;
import de.osthus.esmeralda.IWriter;
import de.osthus.esmeralda.handler.IStatementHandlerExtension;
import de.osthus.esmeralda.handler.IStatementHandlerRegistry;
import de.osthus.esmeralda.handler.csharp.ICsharpHelper;
import de.osthus.esmeralda.misc.Lang;
import de.osthus.esmeralda.snippet.ISnippetManager;

public class CsForEnhancedHandler implements IStatementHandlerExtension<JCEnhancedForLoop>
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
	public void handle(JCEnhancedForLoop tree)
	{
		IConversionContext context = this.context.getCurrent();
		ISnippetManager snippetManager = context.getSnippetManager();
		IWriter writer = context.getWriter();

		languageHelper.newLineIntend();
		writer.append("foreach (");

		StatementTree statement = tree.getStatement();
		Kind kind = statement.getKind();
		IStatementHandlerExtension<StatementTree> stmtHandler = statementHandlerRegistry.get(Lang.C_SHARP + kind);
		if (stmtHandler != null)
		{
			stmtHandler.handle(statement);
		}
		else
		{
			String statementString = statement.toString();
			List<String> untranslatableStatements = Collections.singletonList(statementString);
			snippetManager.writeSnippet(untranslatableStatements);
		}
	}
}
