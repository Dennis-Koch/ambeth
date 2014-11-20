package de.osthus.esmeralda.handler.csharp.stmt;

import com.sun.tools.javac.tree.JCTree;
import com.sun.tools.javac.tree.JCTree.JCExpression;
import com.sun.tools.javac.tree.JCTree.JCVariableDecl;

import de.osthus.ambeth.ioc.annotation.Autowired;
import de.osthus.ambeth.log.ILogger;
import de.osthus.ambeth.log.LogInstance;
import de.osthus.esmeralda.IConversionContext;
import de.osthus.esmeralda.IWriter;
import de.osthus.esmeralda.handler.IStatementHandlerExtension;
import de.osthus.esmeralda.handler.csharp.ICsharpHelper;

public class CsVariableHandler implements IStatementHandlerExtension<JCVariableDecl>
{
	@SuppressWarnings("unused")
	@LogInstance
	private ILogger log;

	@Autowired
	protected IConversionContext context;

	@Autowired
	protected ICsharpHelper languageHelper;

	@Override
	public void handle(JCVariableDecl tree)
	{
		IConversionContext context = this.context.getCurrent();
		IWriter writer = context.getWriter();

		languageHelper.newLineIntend();

		boolean firstKeyWord = true;
		JCTree varType = tree.getType();
		firstKeyWord = languageHelper.writeStringIfFalse(" ", firstKeyWord);
		languageHelper.writeType(varType.toString());
		writer.append(' ').append(tree.getName());

		JCExpression initializer = tree.getInitializer();
		if (initializer != null)
		{
			writer.append(" = ");
			languageHelper.writeExpressionTree(initializer);
		}

		writer.append(';');
	}
}
