package de.osthus.esmeralda.handler.csharp.stmt;

import com.sun.tools.javac.tree.JCTree.JCCase;
import com.sun.tools.javac.tree.JCTree.JCStatement;
import com.sun.tools.javac.tree.JCTree.JCSwitch;
import com.sun.tools.javac.util.List;

import de.osthus.ambeth.log.ILogger;
import de.osthus.ambeth.log.LogInstance;
import de.osthus.ambeth.threading.IBackgroundWorkerDelegate;
import de.osthus.esmeralda.IConversionContext;
import de.osthus.esmeralda.ILanguageHelper;
import de.osthus.esmeralda.handler.IStatementHandlerExtension;
import de.osthus.esmeralda.misc.IWriter;

// TODO remove
@Deprecated
public class CsSwitchHandler extends AbstractCsStatementHandler<JCSwitch> implements IStatementHandlerExtension<JCSwitch>
{
	@SuppressWarnings("unused")
	@LogInstance
	private ILogger log;

	@Override
	public void handle(JCSwitch switchStatement, boolean standalone)
	{
		IConversionContext context = this.context.getCurrent();
		ILanguageHelper languageHelper = context.getLanguageHelper();
		IWriter writer = context.getWriter();

		languageHelper.newLineIndent();
		writer.append("switch ");
		languageHelper.writeExpressionTree(switchStatement.getExpression());
		final List<JCCase> cases = switchStatement.getCases();
		languageHelper.scopeIntend(new IBackgroundWorkerDelegate()
		{
			@Override
			public void invoke() throws Throwable
			{
				for (JCCase caseItem : cases)
				{
					handleCaseStatement(caseItem);
				}
			}
		});
	}

	protected void handleCaseStatement(JCCase caseItem)
	{
		IConversionContext context = this.context.getCurrent();
		ILanguageHelper languageHelper = context.getLanguageHelper();
		IWriter writer = context.getWriter();

		final List<JCStatement> statements = caseItem.getStatements();

		languageHelper.newLineIndent();
		writer.append("case ");
		languageHelper.writeExpressionTree(caseItem.getExpression());
		writer.append(':');
		languageHelper.scopeIntend(new IBackgroundWorkerDelegate()
		{
			@Override
			public void invoke() throws Throwable
			{
				for (JCStatement statement : statements)
				{
					handleChildStatement(statement);
				}
			}
		});
	}
}
