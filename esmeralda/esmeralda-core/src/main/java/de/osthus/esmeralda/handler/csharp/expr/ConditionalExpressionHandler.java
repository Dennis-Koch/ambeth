package de.osthus.esmeralda.handler.csharp.expr;

import com.sun.tools.javac.tree.JCTree.JCConditional;

import de.osthus.ambeth.log.ILogger;
import de.osthus.ambeth.log.LogInstance;
import de.osthus.esmeralda.IConversionContext;
import de.osthus.esmeralda.misc.IWriter;

public class ConditionalExpressionHandler extends AbstractExpressionHandler<JCConditional>
{
	@SuppressWarnings("unused")
	@LogInstance
	private ILogger log;

	@Override
	protected void handleExpressionIntern(JCConditional conditional)
	{
		IConversionContext context = this.context.getCurrent();
		IWriter writer = context.getWriter();

		languageHelper.writeExpressionTree(conditional.cond);
		writer.append(" ? ");
		languageHelper.writeExpressionTree(conditional.truepart);
		writer.append(" : ");
		languageHelper.writeExpressionTree(conditional.falsepart);

		// type on stack because of falsePart is considered as the current stack type of the conditional expression
	}
}
