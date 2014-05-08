package de.osthus.ambeth.ioc.hierarchy;

import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import de.osthus.ambeth.collections.IList;
import de.osthus.ambeth.collections.IMap;
import de.osthus.ambeth.ioc.IDisposableBean;
import de.osthus.ambeth.ioc.IInitializingBean;
import de.osthus.ambeth.ioc.IServiceContext;
import de.osthus.ambeth.ioc.RegisterPhaseDelegate;
import de.osthus.ambeth.log.ILogger;
import de.osthus.ambeth.log.LogInstance;
import de.osthus.ambeth.util.ParamChecker;

public abstract class AbstractChildContextHandle implements IInitializingBean, IContextHandle, IDisposableBean
{
	@LogInstance
	private ILogger log;

	protected IContextFactory contextFactory;

	protected RegisterPhaseDelegate registerPhaseDelegate;

	protected final Lock writeLock = new ReentrantLock();

	@Override
	public void afterPropertiesSet() throws Throwable
	{
		ParamChecker.assertNotNull(contextFactory, "ContextFactory");
	}

	@Override
	public void destroy() throws Throwable
	{
		stop();
	}

	public void setContextFactory(IContextFactory contextFactory)
	{
		this.contextFactory = contextFactory;
	}

	public void setRegisterPhaseDelegate(RegisterPhaseDelegate registerPhaseDelegate)
	{
		this.registerPhaseDelegate = registerPhaseDelegate;
	}

	protected abstract IServiceContext getChildContext();

	protected abstract void setChildContext(IServiceContext childContext);

	@Override
	public IServiceContext start()
	{
		IServiceContext childContext = null;
		Lock writeLock = this.writeLock;
		writeLock.lock();
		try
		{
			if (log.isDebugEnabled())
			{
				log.debug("Looking for existing child context...");
			}
			childContext = getChildContext();
			if (childContext == null || childContext.isDisposed())
			{
				if (log.isDebugEnabled())
				{
					log.debug("No valid child context found. Creating new child context");
				}
				childContext = contextFactory.createChildContext(registerPhaseDelegate);
				setChildContext(childContext);
			}
			else if (log.isDebugEnabled())
			{
				log.debug("Existing child context found and valid");
			}
			IList<IUpwakingBean> upwakingBeans = childContext.getImplementingObjects(IUpwakingBean.class);
			for (int a = 0, size = upwakingBeans.size(); a < size; a++)
			{
				upwakingBeans.get(a).wakeUp();
			}
		}
		finally
		{
			writeLock.unlock();
		}
		return childContext;
	}

	@Override
	public IServiceContext start(final IMap<String, Object> namedBeans)
	{
		throw new UnsupportedOperationException();
	}

	@Override
	public IServiceContext start(RegisterPhaseDelegate registerPhaseDelegate)
	{
		if (registerPhaseDelegate == null)
		{
			return start();
		}
		throw new UnsupportedOperationException();
	}

	@Override
	public void stop()
	{
		writeLock.lock();
		try
		{
			IServiceContext childContext = getChildContext();
			if (childContext != null)
			{
				childContext.dispose();
				setChildContext(null);
			}
		}
		finally
		{
			writeLock.unlock();
		}
	}
}
