package de.osthus.esmeralda.handler.uni.stmt;

import com.sun.tools.javac.tree.JCTree.JCIf;
import com.sun.tools.javac.tree.JCTree.JCStatement;

import de.osthus.ambeth.log.ILogger;
import de.osthus.ambeth.log.LogInstance;
import de.osthus.esmeralda.IConversionContext;
import de.osthus.esmeralda.ILanguageHelper;
import de.osthus.esmeralda.handler.AbstractStatementHandler;
import de.osthus.esmeralda.handler.IStatementHandlerExtension;
import de.osthus.esmeralda.misc.IWriter;

public class UniversalIfHandler extends AbstractStatementHandler<JCIf> implements IStatementHandlerExtension<JCIf>
{
	@SuppressWarnings("unused")
	@LogInstance
	private ILogger log;

	@Override
	public void handle(JCIf ifStatement, boolean standalone)
	{
		IConversionContext context = this.context.getCurrent();
		ILanguageHelper languageHelper = context.getLanguageHelper();
		IWriter writer = context.getWriter();

		if (standalone)
		{
			languageHelper.newLineIndent();
		}

		writer.append("if ");
		languageHelper.writeExpressionTree(ifStatement.getCondition());
		languageHelper.preBlockWhiteSpaces();

		JCStatement thenStatement = ifStatement.getThenStatement();
		handleChildStatement(thenStatement);

		JCStatement elseStatement = ifStatement.getElseStatement();
		if (elseStatement != null)
		{
			languageHelper.postBlockWhiteSpaces();
			writer.append("else");
			if (elseStatement instanceof JCIf)
			{
				writer.append(" ");
				handle((JCIf) elseStatement, false);
			}
			else
			{
				languageHelper.preBlockWhiteSpaces();
				handleChildStatement(elseStatement);
			}
		}
	}
}
