package de.osthus.ambeth.persistence.jdbc;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.sql.Connection;

import net.sf.cglib.proxy.MethodProxy;
import de.osthus.ambeth.exception.RuntimeExceptionUtil;
import de.osthus.ambeth.ioc.threadlocal.Forkable;
import de.osthus.ambeth.ioc.threadlocal.IThreadLocalCleanupBean;
import de.osthus.ambeth.log.ILogger;
import de.osthus.ambeth.log.LogInstance;
import de.osthus.ambeth.merge.ILightweightTransaction;
import de.osthus.ambeth.proxy.AbstractSimpleInterceptor;
import de.osthus.ambeth.threading.SensitiveThreadLocal;

public class ConnectionHolderInterceptor extends AbstractSimpleInterceptor implements IConnectionHolder, IThreadLocalCleanupBean
{
	@SuppressWarnings("unused")
	@LogInstance
	private ILogger log;

	@Forkable
	protected final ThreadLocal<Connection> connectionTL = new SensitiveThreadLocal<Connection>();

	@Override
	public void cleanupThreadLocal()
	{
		if (connectionTL.get() != null)
		{
			throw new IllegalStateException("At this point the thread-local connection has to be already cleaned up gracefully");
		}
	}

	@Override
	public void setConnection(Connection connection)
	{
		Connection oldConnection = connectionTL.get();
		if (oldConnection != null && connection != null && oldConnection != connection)
		{
			throw new IllegalStateException("Thread-local connection instance already applied!. This is a fatal state");
		}
		if (connection == null)
		{
			connectionTL.remove();
		}
		else
		{
			connectionTL.set(connection);
		}
	}

	@Override
	protected Object interceptIntern(Object obj, Method method, Object[] args, MethodProxy proxy) throws Throwable
	{
		try
		{
			Connection connection = getConnection();
			if (connection == null)
			{
				throw new IllegalStateException("No connection currently applied. This often occurs if a " + Connection.class.getName()
						+ "-bean is used without scoping the call through the " + ILightweightTransaction.class.getName() + "-bean");
			}
			return proxy.invoke(connection, args);
		}
		catch (InvocationTargetException e)
		{
			throw RuntimeExceptionUtil.mask(e, method.getExceptionTypes());
		}
	}

	@Override
	public Connection getConnection()
	{
		return connectionTL.get();
	}
}
