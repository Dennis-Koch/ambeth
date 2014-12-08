package de.osthus.esmeralda.handler.csharp.stmt;

import com.sun.source.tree.StatementTree;
import com.sun.tools.javac.tree.JCTree.JCExpression;
import com.sun.tools.javac.tree.JCTree.JCWhileLoop;

import de.osthus.ambeth.log.ILogger;
import de.osthus.ambeth.log.LogInstance;
import de.osthus.esmeralda.IConversionContext;
import de.osthus.esmeralda.handler.IStatementHandlerExtension;
import de.osthus.esmeralda.misc.IWriter;

public class CsWhileHandler extends AbstractCsStatementHandler<JCWhileLoop> implements IStatementHandlerExtension<JCWhileLoop>
{
	@SuppressWarnings("unused")
	@LogInstance
	private ILogger log;

	@Override
	public void handle(JCWhileLoop tree, boolean standalone)
	{
		IConversionContext context = this.context.getCurrent();
		IWriter writer = context.getWriter();

		languageHelper.newLineIndent();
		writer.append("while ");
		JCExpression condition = tree.getCondition();
		languageHelper.writeExpressionTree(condition);

		StatementTree statement = tree.getStatement();
		handleChildStatement(statement);
	}
}
