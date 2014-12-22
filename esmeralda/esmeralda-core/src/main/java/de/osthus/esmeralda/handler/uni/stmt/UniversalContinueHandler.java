package de.osthus.esmeralda.handler.uni.stmt;

import com.sun.tools.javac.tree.JCTree.JCContinue;

import de.osthus.ambeth.log.ILogger;
import de.osthus.ambeth.log.LogInstance;
import de.osthus.esmeralda.IConversionContext;
import de.osthus.esmeralda.ILanguageHelper;
import de.osthus.esmeralda.handler.AbstractStatementHandler;
import de.osthus.esmeralda.handler.IStatementHandlerExtension;
import de.osthus.esmeralda.misc.IWriter;

public class UniversalContinueHandler extends AbstractStatementHandler<JCContinue> implements IStatementHandlerExtension<JCContinue>
{
	@SuppressWarnings("unused")
	@LogInstance
	private ILogger log;

	@Override
	public void handle(JCContinue continueStatement, boolean standalone)
	{
		if (continueStatement.label != null)
		{
			log.warn("Continue with label is not yet supported: " + continueStatement);
			return;
		}

		IConversionContext context = this.context.getCurrent();
		ILanguageHelper languageHelper = context.getLanguageHelper();
		IWriter writer = context.getWriter();

		languageHelper.newLineIndent();
		writer.append("continue;");
	}
}
