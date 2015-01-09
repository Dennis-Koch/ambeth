package de.osthus.esmeralda.handler.csharp.stmt;

import com.sun.tools.javac.tree.JCTree.JCAssert;

import de.osthus.ambeth.log.ILogger;
import de.osthus.ambeth.log.LogInstance;
import de.osthus.esmeralda.IConversionContext;
import de.osthus.esmeralda.ILanguageHelper;
import de.osthus.esmeralda.handler.IStatementHandlerExtension;
import de.osthus.esmeralda.misc.IWriter;

public class CsAssertHandler extends AbstractCsStatementHandler<JCAssert> implements IStatementHandlerExtension<JCAssert>
{
	@SuppressWarnings("unused")
	@LogInstance
	private ILogger log;

	@Override
	public void handle(JCAssert assertStatement, boolean standalone)
	{
		IConversionContext context = this.context.getCurrent();
		ILanguageHelper languageHelper = context.getLanguageHelper();
		IWriter writer = context.getWriter();

		languageHelper.writeType("System.Diagnostics.Debug");
		writer.append(".Assert(");
		languageHelper.writeExpressionTree(assertStatement.getCondition());
		writer.append(", ");
		languageHelper.writeExpressionTree(assertStatement.getDetail());
		writer.append(");");
	}
}
