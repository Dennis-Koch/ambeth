package de.osthus.esmeralda.handler.js.stmt;

import com.sun.tools.javac.tree.JCTree.JCAssert;

import de.osthus.ambeth.log.ILogger;
import de.osthus.ambeth.log.LogInstance;
import de.osthus.esmeralda.IConversionContext;
import de.osthus.esmeralda.ILanguageHelper;
import de.osthus.esmeralda.handler.IStatementHandlerExtension;
import de.osthus.esmeralda.misc.IWriter;

public class JsAssertHandler extends AbstractJsStatementHandler<JCAssert> implements IStatementHandlerExtension<JCAssert>
{
	@SuppressWarnings("unused")
	@LogInstance
	private ILogger log;

	@Override
	public void handle(JCAssert assertStatement, boolean standalone)
	{
		IConversionContext context = this.context.getCurrent();
		ILanguageHelper languageHelper = context.getLanguageHelper();
		IWriter writer = context.getWriter();

		// TODO: asserts possible in javascript?
	}
}
