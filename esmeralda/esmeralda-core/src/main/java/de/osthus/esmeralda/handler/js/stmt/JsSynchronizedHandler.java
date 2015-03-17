package de.osthus.esmeralda.handler.js.stmt;

import com.sun.tools.javac.tree.JCTree.JCSynchronized;

import de.osthus.ambeth.ioc.annotation.Autowired;
import de.osthus.ambeth.log.ILogger;
import de.osthus.ambeth.log.LogInstance;
import de.osthus.esmeralda.handler.IStatementHandlerExtension;
import de.osthus.esmeralda.handler.uni.stmt.UniversalBlockHandler;

public class JsSynchronizedHandler extends AbstractJsStatementHandler<JCSynchronized> implements IStatementHandlerExtension<JCSynchronized>
{
	@SuppressWarnings("unused")
	@LogInstance
	private ILogger log;

	@Autowired
	protected UniversalBlockHandler blockHandler;

	@Override
	public void handle(JCSynchronized tree, boolean standalone)
	{
		blockHandler.writeBlockContentWithoutIntendation(tree.getBlock());
	}
}
