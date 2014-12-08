package de.osthus.esmeralda.handler.csharp.expr;

import com.sun.tools.javac.tree.JCTree.JCConditional;

import de.osthus.ambeth.log.ILogger;
import de.osthus.ambeth.log.LogInstance;
import de.osthus.esmeralda.IConversionContext;
import de.osthus.esmeralda.ILanguageHelper;
import de.osthus.esmeralda.handler.AbstractExpressionHandler;
import de.osthus.esmeralda.misc.IWriter;

public class ConditionalExpressionHandler extends AbstractExpressionHandler<JCConditional>
{
	@SuppressWarnings("unused")
	@LogInstance
	private ILogger log;

	@Override
	protected void handleExpressionIntern(JCConditional expression)
	{
		IConversionContext context = this.context.getCurrent();
		ILanguageHelper languageHelper = context.getLanguageHelper();
		IWriter writer = context.getWriter();

		languageHelper.writeExpressionTree(expression.getCondition());
		writer.append(" ? ");
		languageHelper.writeExpressionTree(expression.getTrueExpression());
		writer.append(" : ");
		languageHelper.writeExpressionTree(expression.getFalseExpression());
	}
}
