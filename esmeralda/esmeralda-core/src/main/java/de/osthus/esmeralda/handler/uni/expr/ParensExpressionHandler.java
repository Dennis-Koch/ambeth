package de.osthus.esmeralda.handler.uni.expr;

import com.sun.tools.javac.tree.JCTree.JCParens;

import de.osthus.ambeth.log.ILogger;
import de.osthus.ambeth.log.LogInstance;
import de.osthus.esmeralda.IConversionContext;
import de.osthus.esmeralda.ILanguageHelper;
import de.osthus.esmeralda.handler.AbstractExpressionHandler;
import de.osthus.esmeralda.misc.IWriter;

public class ParensExpressionHandler extends AbstractExpressionHandler<JCParens>
{
	@SuppressWarnings("unused")
	@LogInstance
	private ILogger log;

	@Override
	protected void handleExpressionIntern(JCParens parens)
	{
		IConversionContext context = this.context.getCurrent();
		ILanguageHelper languageHelper = context.getLanguageHelper();
		IWriter writer = context.getWriter();

		writer.append('(');
		languageHelper.writeExpressionTree(parens.getExpression());
		writer.append(')');
	}
}
