package de.osthus.esmeralda.handler.js.stmt;

import com.sun.tools.javac.tree.JCTree.JCForLoop;

import de.osthus.ambeth.log.ILogger;
import de.osthus.ambeth.log.LogInstance;
import de.osthus.esmeralda.handler.IStatementHandlerExtension;
import de.osthus.esmeralda.handler.csharp.stmt.CsForHandler;
import de.osthus.esmeralda.misc.Lang;

public class JsForHandler extends CsForHandler implements IStatementHandlerExtension<JCForLoop>
{
	@SuppressWarnings("unused")
	@LogInstance
	private ILogger log;

	public JsForHandler()
	{
		// Overwrite CS from super constructor
		language = Lang.JS;
	}
}
