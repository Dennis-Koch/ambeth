package de.osthus.esmeralda.handler.js.stmt;

import com.sun.tools.javac.tree.JCTree.JCIf;

import de.osthus.ambeth.log.ILogger;
import de.osthus.ambeth.log.LogInstance;
import de.osthus.esmeralda.handler.IStatementHandlerExtension;
import de.osthus.esmeralda.handler.csharp.stmt.CsIfHandler;
import de.osthus.esmeralda.misc.Lang;

public class JsIfHandler extends CsIfHandler implements IStatementHandlerExtension<JCIf>
{
	@SuppressWarnings("unused")
	@LogInstance
	private ILogger log;

	public JsIfHandler()
	{
		// Overwrite CS from super constructor
		language = Lang.JS;
	}
}
