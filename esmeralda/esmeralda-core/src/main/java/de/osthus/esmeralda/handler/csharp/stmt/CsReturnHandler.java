package de.osthus.esmeralda.handler.csharp.stmt;

import com.sun.tools.javac.tree.JCTree.JCExpression;
import com.sun.tools.javac.tree.JCTree.JCReturn;

import de.osthus.ambeth.log.ILogger;
import de.osthus.ambeth.log.LogInstance;
import de.osthus.esmeralda.IConversionContext;
import de.osthus.esmeralda.IWriter;
import de.osthus.esmeralda.handler.IStatementHandlerExtension;

public class CsReturnHandler extends AbstractStatementHandler<JCReturn> implements IStatementHandlerExtension<JCReturn>
{
	@SuppressWarnings("unused")
	@LogInstance
	private ILogger log;

	@Override
	public void handle(JCReturn tree, boolean standalone)
	{
		IConversionContext context = this.context.getCurrent();
		IWriter writer = context.getWriter();

		languageHelper.newLineIntend();

		writer.append("return ");

		JCExpression initializer = tree.getExpression();
		if (initializer != null)
		{
			writer.append(" = ");
			languageHelper.writeExpressionTree(initializer);
		}

		writer.append(';');
	}
}
