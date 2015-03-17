package de.osthus.esmeralda.handler.js.stmt;

import com.sun.tools.javac.tree.JCTree.JCAssert;

import de.osthus.ambeth.log.ILogger;
import de.osthus.ambeth.log.LogInstance;
import de.osthus.esmeralda.handler.IStatementHandlerExtension;

public class JsAssertHandler extends AbstractJsStatementHandler<JCAssert> implements IStatementHandlerExtension<JCAssert>
{
	@SuppressWarnings("unused")
	@LogInstance
	private ILogger log;

	@Override
	public void handle(JCAssert assertStatement, boolean standalone)
	{
		// TODO: asserts possible in javascript?
	}
}
