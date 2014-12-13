package de.osthus.esmeralda.handler;

import com.sun.source.tree.Tree;

import de.osthus.ambeth.ioc.annotation.Autowired;
import de.osthus.ambeth.log.ILogger;
import de.osthus.ambeth.log.LogInstance;
import de.osthus.ambeth.objectcollector.IThreadLocalObjectCollector;
import de.osthus.esmeralda.IConversionContext;

public abstract class AbstractExpressionHandler<T> implements IExpressionHandler
{
	@SuppressWarnings("unused")
	@LogInstance
	private ILogger log;

	@Autowired
	protected IASTHelper astHelper;

	@Autowired
	protected IConversionContext context;

	@Autowired
	protected IThreadLocalObjectCollector objectCollector;

	@Autowired
	protected IStatementHandlerRegistry statementHandlerRegistry;

	@SuppressWarnings("unchecked")
	@Override
	public void handleExpression(Tree expression)
	{
		handleExpressionIntern((T) expression);
	}

	protected abstract void handleExpressionIntern(T expression);
}
