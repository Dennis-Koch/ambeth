package de.osthus.esmeralda.handler.csharp;

import com.sun.tools.javac.tree.JCTree.JCAssign;

import de.osthus.ambeth.log.ILogger;
import de.osthus.ambeth.log.LogInstance;
import de.osthus.esmeralda.IConversionContext;
import de.osthus.esmeralda.IWriter;

public class AssignExpressionHandler extends AbstractExpressionHandler<JCAssign>
{
	@SuppressWarnings("unused")
	@LogInstance
	private ILogger log;

	@Override
	protected void handleExpressionIntern(JCAssign expression)
	{
		IConversionContext context = this.context.getCurrent();
		IWriter writer = context.getWriter();

		languageHelper.writeExpressionTree(expression.lhs);
		writer.append(" = ");
		languageHelper.writeExpressionTree(expression.rhs);
	}
}
