package de.osthus.esmeralda.handler.uni.stmt;

import com.sun.tools.javac.tree.JCTree.JCLabeledStatement;

import de.osthus.ambeth.log.ILogger;
import de.osthus.ambeth.log.LogInstance;
import de.osthus.esmeralda.IConversionContext;
import de.osthus.esmeralda.ILanguageHelper;
import de.osthus.esmeralda.handler.AbstractStatementHandler;
import de.osthus.esmeralda.handler.IStatementHandlerExtension;
import de.osthus.esmeralda.misc.IWriter;

public class UniversalLabeledStatementHandler extends AbstractStatementHandler<JCLabeledStatement> implements IStatementHandlerExtension<JCLabeledStatement>
{
	@SuppressWarnings("unused")
	@LogInstance
	private ILogger log;

	@Override
	public void handle(JCLabeledStatement tree, boolean standalone)
	{
		IConversionContext context = this.context.getCurrent();
		ILanguageHelper languageHelper = context.getLanguageHelper();
		IWriter writer = context.getWriter();

		languageHelper.newLineIndent();
		writer.append(tree.getLabel()).append(": ");
		languageHelper.writeExpressionTree(tree.getStatement());
	}
}
