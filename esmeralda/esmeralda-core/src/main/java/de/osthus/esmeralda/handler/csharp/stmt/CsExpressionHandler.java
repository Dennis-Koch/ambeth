package de.osthus.esmeralda.handler.csharp.stmt;

import com.sun.tools.javac.tree.JCTree.JCExpressionStatement;

import de.osthus.ambeth.log.ILogger;
import de.osthus.ambeth.log.LogInstance;
import de.osthus.ambeth.threading.IBackgroundWorkerDelegate;
import de.osthus.esmeralda.IConversionContext;
import de.osthus.esmeralda.handler.IStatementHandlerExtension;
import de.osthus.esmeralda.misc.IWriter;

public class CsExpressionHandler extends AbstractStatementHandler<JCExpressionStatement> implements IStatementHandlerExtension<JCExpressionStatement>
{
	@SuppressWarnings("unused")
	@LogInstance
	private ILogger log;

	@Override
	public void handle(final JCExpressionStatement tree, boolean standalone)
	{
		IConversionContext context = this.context.getCurrent();
		IWriter writer = context.getWriter();

		String stash = astHelper.writeToStash(new IBackgroundWorkerDelegate()
		{
			@Override
			public void invoke() throws Throwable
			{
				languageHelper.writeExpressionTree(tree.getExpression());
			}
		});

		if (stash.length() == 0)
		{
			return;
		}
		if (standalone)
		{
			languageHelper.newLineIndent();
		}
		writer.append(stash);
		if (standalone)
		{
			writer.append(';');
		}
	}
}
