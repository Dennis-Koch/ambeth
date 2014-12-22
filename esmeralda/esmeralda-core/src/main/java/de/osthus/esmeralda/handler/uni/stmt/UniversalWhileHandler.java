package de.osthus.esmeralda.handler.uni.stmt;

import com.sun.source.tree.StatementTree;
import com.sun.tools.javac.tree.JCTree.JCExpression;
import com.sun.tools.javac.tree.JCTree.JCWhileLoop;

import de.osthus.ambeth.log.ILogger;
import de.osthus.ambeth.log.LogInstance;
import de.osthus.esmeralda.IConversionContext;
import de.osthus.esmeralda.ILanguageHelper;
import de.osthus.esmeralda.handler.IStatementHandlerExtension;
import de.osthus.esmeralda.handler.csharp.stmt.AbstractCsStatementHandler;
import de.osthus.esmeralda.misc.IWriter;

public class UniversalWhileHandler extends AbstractCsStatementHandler<JCWhileLoop> implements IStatementHandlerExtension<JCWhileLoop>
{
	@SuppressWarnings("unused")
	@LogInstance
	private ILogger log;

	@Override
	public void handle(JCWhileLoop tree, boolean standalone)
	{
		IConversionContext context = this.context.getCurrent();
		ILanguageHelper languageHelper = context.getLanguageHelper();
		IWriter writer = context.getWriter();

		languageHelper.newLineIndent();
		writer.append("while ");
		JCExpression condition = tree.getCondition();
		languageHelper.writeExpressionTree(condition);
		languageHelper.preBlockWhiteSpaces();

		StatementTree statement = tree.getStatement();
		handleChildStatement(statement);
	}
}
