package de.osthus.esmeralda.handler.csharp.stmt;

import com.sun.tools.javac.tree.JCTree.JCIf;
import com.sun.tools.javac.tree.JCTree.JCStatement;

import de.osthus.ambeth.log.ILogger;
import de.osthus.ambeth.log.LogInstance;
import de.osthus.esmeralda.IConversionContext;
import de.osthus.esmeralda.ILanguageHelper;
import de.osthus.esmeralda.handler.IStatementHandlerExtension;
import de.osthus.esmeralda.misc.IWriter;

public class CsIfHandler extends AbstractCsStatementHandler<JCIf> implements IStatementHandlerExtension<JCIf>
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

		JCStatement thenStatement = ifStatement.getThenStatement();
		handleChildStatement(thenStatement);

		JCStatement elseStatement = ifStatement.getElseStatement();
		if (elseStatement != null)
		{
			languageHelper.newLineIndent();
			writer.append("else");
			if (elseStatement instanceof JCIf)
			{
				writer.append(" ");
				handle((JCIf) elseStatement, false);
			}
			else
			{
				handleChildStatement(elseStatement);
			}
		}
	}
}
