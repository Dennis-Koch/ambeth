package de.osthus.esmeralda.handler.csharp.expr;

import com.sun.tools.javac.tree.JCTree.JCArrayTypeTree;

import de.osthus.ambeth.log.ILogger;
import de.osthus.ambeth.log.LogInstance;

public class ArrayTypeExpressionHandler extends AbstractExpressionHandler<JCArrayTypeTree>
{
	@SuppressWarnings("unused")
	@LogInstance
	private ILogger log;

	@Override
	protected void handleExpressionIntern(JCArrayTypeTree arrayType)
	{
		languageHelper.writeType(arrayType.type.toString());
	}
}
