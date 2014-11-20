package de.osthus.esmeralda.handler.csharp.stmt;

import com.sun.tools.javac.tree.JCTree.JCBreak;

import de.osthus.ambeth.log.ILogger;
import de.osthus.ambeth.log.LogInstance;
import de.osthus.esmeralda.IConversionContext;
import de.osthus.esmeralda.handler.IStatementHandlerExtension;
import de.osthus.esmeralda.misc.IWriter;

public class CsBreakHandler extends AbstractStatementHandler<JCBreak> implements IStatementHandlerExtension<JCBreak>
{
	@SuppressWarnings("unused")
	@LogInstance
	private ILogger log;

	@Override
	public void handle(JCBreak breakStatement, boolean standalone)
	{
		if (breakStatement.label != null)
		{
			log.warn("Continue with label is not yet supported: " + breakStatement);
			return;
		}
		IConversionContext context = this.context.getCurrent();
		IWriter writer = context.getWriter();

		languageHelper.newLineIntend();
		writer.append("break;");
	}
}
