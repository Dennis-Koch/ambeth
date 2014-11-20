package de.osthus.esmeralda.handler.csharp.stmt;

import com.sun.tools.javac.tree.JCTree.JCExpressionStatement;

import de.osthus.ambeth.ioc.annotation.Autowired;
import de.osthus.ambeth.log.ILogger;
import de.osthus.ambeth.log.LogInstance;
import de.osthus.esmeralda.IConversionContext;
import de.osthus.esmeralda.IWriter;
import de.osthus.esmeralda.handler.IStatementHandlerExtension;
import de.osthus.esmeralda.handler.csharp.ICsharpHelper;

public class CsExpressionHandler implements IStatementHandlerExtension<JCExpressionStatement>
{
	@SuppressWarnings("unused")
	@LogInstance
	private ILogger log;

	@Autowired
	protected IConversionContext context;

	@Autowired
	protected ICsharpHelper languageHelper;

	@Override
	public void handle(JCExpressionStatement tree)
	{
		IConversionContext context = this.context.getCurrent();
		IWriter writer = context.getWriter();

		languageHelper.newLineIntend();
		languageHelper.writeExpressionTree(tree.getExpression());
		writer.append(';');
	}
}
