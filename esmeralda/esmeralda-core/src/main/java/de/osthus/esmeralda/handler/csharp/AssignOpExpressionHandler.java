package de.osthus.esmeralda.handler.csharp;

import com.sun.tools.javac.tree.JCTree.JCAssignOp;

import de.osthus.ambeth.log.ILogger;
import de.osthus.ambeth.log.LogInstance;
import de.osthus.esmeralda.IConversionContext;
import de.osthus.esmeralda.IWriter;

public class AssignOpExpressionHandler extends AbstractExpressionHandler<JCAssignOp>
{
	@SuppressWarnings("unused")
	@LogInstance
	private ILogger log;

	@Override
	protected void handleExpressionIntern(JCAssignOp expression)
	{
		IConversionContext context = this.context.getCurrent();
		IWriter writer = context.getWriter();

		languageHelper.writeExpressionTree(expression.lhs);
		writer.append(" ").append(expression.operator.name).append("= ");
		languageHelper.writeExpressionTree(expression.rhs);
	}
}
