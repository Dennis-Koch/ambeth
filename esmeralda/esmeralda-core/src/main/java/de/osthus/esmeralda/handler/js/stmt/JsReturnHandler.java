package de.osthus.esmeralda.handler.js.stmt;

import com.sun.tools.javac.tree.JCTree.JCReturn;

import de.osthus.ambeth.log.ILogger;
import de.osthus.ambeth.log.LogInstance;
import de.osthus.esmeralda.handler.IStatementHandlerExtension;
import de.osthus.esmeralda.handler.csharp.stmt.CsReturnHandler;
import de.osthus.esmeralda.handler.js.IJsHelper;
import de.osthus.esmeralda.misc.Lang;

public class JsReturnHandler extends CsReturnHandler implements IStatementHandlerExtension<JCReturn>
{
	@SuppressWarnings("unused")
	@LogInstance
	private ILogger log;

	public JsReturnHandler()
	{
		language = Lang.JS;
	}

	public void setLanguageHelper(IJsHelper languageHelper)
	{
		this.languageHelper = languageHelper;
	}
}
