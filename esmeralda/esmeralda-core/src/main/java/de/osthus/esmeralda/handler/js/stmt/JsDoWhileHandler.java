package de.osthus.esmeralda.handler.js.stmt;

import de.osthus.ambeth.log.ILogger;
import de.osthus.ambeth.log.LogInstance;
import de.osthus.esmeralda.handler.csharp.stmt.CsDoWhileHandler;
import de.osthus.esmeralda.misc.Lang;

public class JsDoWhileHandler extends CsDoWhileHandler
{
	@SuppressWarnings("unused")
	@LogInstance
	private ILogger log;

	public JsDoWhileHandler()
	{
		// Overwrite CS from super constructor
		language = Lang.JS;
	}
}
