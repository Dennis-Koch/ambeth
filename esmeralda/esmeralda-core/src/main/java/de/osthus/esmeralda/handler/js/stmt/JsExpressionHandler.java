package de.osthus.esmeralda.handler.js.stmt;

import com.sun.tools.javac.tree.JCTree.JCExpressionStatement;

import de.osthus.ambeth.log.ILogger;
import de.osthus.ambeth.log.LogInstance;
import de.osthus.esmeralda.handler.IStatementHandlerExtension;
import de.osthus.esmeralda.handler.csharp.stmt.CsExpressionHandler;
import de.osthus.esmeralda.misc.Lang;

public class JsExpressionHandler extends CsExpressionHandler implements IStatementHandlerExtension<JCExpressionStatement>
{
	@SuppressWarnings("unused")
	@LogInstance
	private ILogger log;

	public JsExpressionHandler()
	{
		// Overwrite CS from super constructor
		language = Lang.JS;
	}
}
