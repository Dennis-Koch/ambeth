package de.osthus.esmeralda.handler.csharp.stmt;

import com.sun.tools.javac.tree.JCTree.JCIf;
import com.sun.tools.javac.tree.JCTree.JCStatement;

import de.osthus.ambeth.log.ILogger;
import de.osthus.ambeth.log.LogInstance;
import de.osthus.ambeth.threading.IBackgroundWorkerDelegate;
import de.osthus.esmeralda.IConversionContext;
import de.osthus.esmeralda.handler.IStatementHandlerExtension;
import de.osthus.esmeralda.misc.IWriter;

public class CsIfHandler extends AbstractStatementHandler<JCIf> implements IStatementHandlerExtension<JCIf>
{
	@SuppressWarnings("unused")
	@LogInstance
	private ILogger log;

	@Override
	public void handle(JCIf ifStatement, boolean standalone)
	{
		IConversionContext context = this.context.getCurrent();
		IWriter writer = context.getWriter();

		final JCStatement thenStatement = ifStatement.getThenStatement();
		final JCStatement elseStatement = ifStatement.getElseStatement();
		languageHelper.newLineIntend();
		writer.append("if (");
		languageHelper.writeExpressionTree(ifStatement.getCondition());
		writer.append(')');
		languageHelper.scopeIntend(buildThenOrElse(thenStatement));
		if (elseStatement != null)
		{
			languageHelper.scopeIntend(buildThenOrElse(elseStatement));
		}
	}

	protected IBackgroundWorkerDelegate buildThenOrElse(final JCStatement statement)
	{
		return new IBackgroundWorkerDelegate()
		{
			@Override
			public void invoke() throws Throwable
			{
				IConversionContext context = CsIfHandler.this.context.getCurrent();
				IWriter writer = context.getWriter();
				if (statement == null)
				{
					writer.append(INTENDED_BLANK);
				}
				else
				{
					handleChildStatement(statement);
				}
			}
		};
	}
}
