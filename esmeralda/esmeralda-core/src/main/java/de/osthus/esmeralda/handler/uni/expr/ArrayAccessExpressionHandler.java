package de.osthus.esmeralda.handler.uni.expr;

import com.sun.tools.javac.tree.JCTree.JCArrayAccess;

import de.osthus.ambeth.log.ILogger;
import de.osthus.ambeth.log.LogInstance;
import de.osthus.esmeralda.IConversionContext;
import de.osthus.esmeralda.ILanguageHelper;
import de.osthus.esmeralda.handler.AbstractExpressionHandler;
import de.osthus.esmeralda.misc.IWriter;

public class ArrayAccessExpressionHandler extends AbstractExpressionHandler<JCArrayAccess>
{
	@SuppressWarnings("unused")
	@LogInstance
	private ILogger log;

	@Override
	protected void handleExpressionIntern(JCArrayAccess expression)
	{
		IConversionContext context = this.context.getCurrent();
		ILanguageHelper languageHelper = context.getLanguageHelper();
		IWriter writer = context.getWriter();

		languageHelper.writeExpressionTree(expression.getExpression());
		String typeOnStack = context.getTypeOnStack();
		if (typeOnStack.endsWith("[]"))
		{
			typeOnStack = typeOnStack.substring(0, typeOnStack.length() - 2);
		}
		writer.append('[');
		languageHelper.writeExpressionTree(expression.getIndex());
		writer.append(']');

		context.setTypeOnStack(typeOnStack);
	}
}
