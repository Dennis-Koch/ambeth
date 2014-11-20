package de.osthus.esmeralda.handler.csharp;

import com.sun.source.tree.ExpressionTree;

import de.osthus.ambeth.ioc.annotation.Autowired;
import de.osthus.ambeth.log.ILogger;
import de.osthus.ambeth.log.LogInstance;
import de.osthus.ambeth.objectcollector.IThreadLocalObjectCollector;
import de.osthus.esmeralda.IConversionContext;
import de.osthus.esmeralda.handler.IExpressionHandler;

public abstract class AbstractExpressionHandler<T> implements IExpressionHandler
{
	@SuppressWarnings("unused")
	@LogInstance
	private ILogger log;

	@Autowired
	protected IConversionContext context;

	@Autowired
	protected ICsharpHelper languageHelper;

	@Autowired
	protected IThreadLocalObjectCollector objectCollector;

	@SuppressWarnings("unchecked")
	@Override
	public void handleExpression(ExpressionTree expression)
	{
		handleExpressionIntern((T) expression);
	}

	protected abstract void handleExpressionIntern(T expression);
}
