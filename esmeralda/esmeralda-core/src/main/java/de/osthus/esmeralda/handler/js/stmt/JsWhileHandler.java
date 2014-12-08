package de.osthus.esmeralda.handler.js.stmt;

import de.osthus.ambeth.log.ILogger;
import de.osthus.ambeth.log.LogInstance;
import de.osthus.esmeralda.handler.csharp.stmt.CsWhileHandler;
import de.osthus.esmeralda.misc.Lang;

public class JsWhileHandler extends CsWhileHandler
{
	@SuppressWarnings("unused")
	@LogInstance
	private ILogger log;

	public JsWhileHandler()
	{
		// Overwrite CS from super constructor
		language = Lang.JS;
	}
}
