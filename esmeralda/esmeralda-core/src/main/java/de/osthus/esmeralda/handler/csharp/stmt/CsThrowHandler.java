package de.osthus.esmeralda.handler.csharp.stmt;

import com.sun.tools.javac.tree.JCTree.JCThrow;

import de.osthus.ambeth.log.ILogger;
import de.osthus.ambeth.log.LogInstance;
import de.osthus.esmeralda.IConversionContext;
import de.osthus.esmeralda.ILanguageHelper;
import de.osthus.esmeralda.handler.IStatementHandlerExtension;
import de.osthus.esmeralda.misc.IWriter;

public class CsThrowHandler extends AbstractCsStatementHandler<JCThrow> implements IStatementHandlerExtension<JCThrow>
{
	@SuppressWarnings("unused")
	@LogInstance
	private ILogger log;

	@Override
	public void handle(JCThrow throwStatement, boolean standalone)
	{
		IConversionContext context = this.context.getCurrent();
		ILanguageHelper languageHelper = context.getLanguageHelper();
		IWriter writer = context.getWriter();

		if (standalone)
		{
			languageHelper.newLineIndent();
		}
		writer.append("throw ");
		languageHelper.writeExpressionTree(throwStatement.getExpression());

		if (standalone)
		{
			writer.append(';');
		}
	}
}
