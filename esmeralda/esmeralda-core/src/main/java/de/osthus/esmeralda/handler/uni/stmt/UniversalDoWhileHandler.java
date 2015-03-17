package de.osthus.esmeralda.handler.uni.stmt;

import com.sun.source.tree.StatementTree;
import com.sun.tools.javac.tree.JCTree.JCDoWhileLoop;
import com.sun.tools.javac.tree.JCTree.JCExpression;

import de.osthus.ambeth.log.ILogger;
import de.osthus.ambeth.log.LogInstance;
import de.osthus.esmeralda.IConversionContext;
import de.osthus.esmeralda.ILanguageHelper;
import de.osthus.esmeralda.handler.AbstractStatementHandler;
import de.osthus.esmeralda.handler.IStatementHandlerExtension;
import de.osthus.esmeralda.misc.IWriter;

public class UniversalDoWhileHandler extends AbstractStatementHandler<JCDoWhileLoop> implements IStatementHandlerExtension<JCDoWhileLoop>
{
	@SuppressWarnings("unused")
	@LogInstance
	private ILogger log;

	@Override
	public void handle(JCDoWhileLoop tree, boolean standalone)
	{
		IConversionContext context = this.context.getCurrent();
		ILanguageHelper languageHelper = context.getLanguageHelper();
		IWriter writer = context.getWriter();

		languageHelper.newLineIndent();
		writer.append("do ");

		StatementTree statement = tree.getStatement();
		handleChildStatement(statement);

		writer.append(" while ");
		JCExpression condition = tree.getCondition();
		languageHelper.writeExpressionTree(condition);
		writer.append(";");
	}
}
