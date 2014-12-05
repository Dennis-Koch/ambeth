package de.osthus.esmeralda.handler.csharp.stmt;

import com.sun.tools.javac.tree.JCTree.JCSynchronized;

import de.osthus.ambeth.log.ILogger;
import de.osthus.ambeth.log.LogInstance;
import de.osthus.esmeralda.IConversionContext;
import de.osthus.esmeralda.handler.IStatementHandlerExtension;
import de.osthus.esmeralda.misc.IWriter;

public class CsSynchronizedHandler extends AbstractStatementHandler<JCSynchronized> implements IStatementHandlerExtension<JCSynchronized>
{
	@SuppressWarnings("unused")
	@LogInstance
	private ILogger log;

	@Override
	public void handle(JCSynchronized tree, boolean standalone)
	{
		IConversionContext context = this.context.getCurrent();
		IWriter writer = context.getWriter();

		languageHelper.newLineIndent();
		writer.append("lock ");
		languageHelper.writeExpressionTree(tree.getExpression());
		languageHelper.writeExpressionTree(tree.getBlock());
	}
}
