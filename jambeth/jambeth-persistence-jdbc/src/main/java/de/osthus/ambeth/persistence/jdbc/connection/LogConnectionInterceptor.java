package de.osthus.ambeth.persistence.jdbc.connection;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Wrapper;

import javax.sql.PooledConnection;

import net.sf.cglib.proxy.MethodInterceptor;
import net.sf.cglib.proxy.MethodProxy;
import de.osthus.ambeth.config.Property;
import de.osthus.ambeth.event.IEventDispatcher;
import de.osthus.ambeth.exception.RuntimeExceptionUtil;
import de.osthus.ambeth.ioc.IServiceContext;
import de.osthus.ambeth.ioc.annotation.Autowired;
import de.osthus.ambeth.log.ILogger;
import de.osthus.ambeth.log.LogInstance;
import de.osthus.ambeth.persistence.SQLState;
import de.osthus.ambeth.persistence.config.PersistenceConfigurationConstants;
import de.osthus.ambeth.persistence.jdbc.event.ConnectionClosedEvent;
import de.osthus.ambeth.proxy.AbstractSimpleInterceptor;
import de.osthus.ambeth.proxy.ICgLibUtil;
import de.osthus.ambeth.proxy.IProxyFactory;
import de.osthus.ambeth.util.IPrintable;

public class LogConnectionInterceptor extends AbstractSimpleInterceptor
{
	public static final Method createStatementMethod;

	public static final Method isClosedMethod, closeMethod, pooledCloseMethod, toStringMethod;

	public static final Method unwrapMethod, isWrapperForMethod;

	static
	{
		try
		{
			toStringMethod = Object.class.getMethod("toString");
			createStatementMethod = Connection.class.getMethod("createStatement");
			isClosedMethod = Connection.class.getMethod("isClosed");
			closeMethod = Connection.class.getMethod("close");
			pooledCloseMethod = PooledConnection.class.getMethod("close");
			unwrapMethod = Wrapper.class.getMethod("unwrap", Class.class);
			isWrapperForMethod = Wrapper.class.getMethod("isWrapperFor", Class.class);
		}
		catch (Throwable e)
		{
			throw RuntimeExceptionUtil.mask(e);
		}
	}

	@SuppressWarnings("unused")
	@LogInstance
	private ILogger log;

	@Autowired
	protected ICgLibUtil cgLibUtil;

	@Autowired(optional = true)
	protected IEventDispatcher eventDispatcher;

	@Autowired
	protected IProxyFactory proxyFactory;

	@Autowired
	protected IServiceContext beanContext;

	@Autowired
	protected Connection connection;

	@Property(name = PersistenceConfigurationConstants.FetchSize, defaultValue = "100")
	protected int fetchSize;

	protected final IConnectionKeyHandle connectionKeyHandle;

	protected Class<?>[] pstmInterfaces;

	protected Class<?>[] stmInterfaces;

	public LogConnectionInterceptor(IConnectionKeyHandle connectionKeyHandle)
	{
		this.connectionKeyHandle = connectionKeyHandle;
	}

	public void setConnection(Connection connection)
	{
		this.connection = connection;
	}

	@Override
	protected Object interceptIntern(Object obj, Method method, Object[] args, MethodProxy proxy) throws Throwable
	{
		if (connection == null)
		{
			if (isClosedMethod.equals(method))
			{
				return Boolean.TRUE;
			}
			else if (toStringMethod.equals(method))
			{
				return toString();
			}
			else if (pooledCloseMethod.equals(method) || closeMethod.equals(method))
			{
				return null;
			}
			throw new SQLException(SQLState.CONNECTION_NOT_OPEN.getMessage(), SQLState.CONNECTION_NOT_OPEN.getXopen());
		}
		if (isWrapperForMethod.equals(method) && IConnectionKeyHandle.class.equals(args[0]))
		{
			return Boolean.TRUE;
		}
		else if (unwrapMethod.equals(method) && IConnectionKeyHandle.class.equals(args[0]))
		{
			return connectionKeyHandle;
		}
		try
		{
			if (PreparedStatement.class.isAssignableFrom(method.getReturnType()))
			{
				PreparedStatement pstm = (PreparedStatement) proxy.invoke(connection, args);

				pstm.setFetchSize(fetchSize);
				MethodInterceptor logPstmInterceptor = beanContext.registerBean(LogPreparedStatementInterceptor.class)//
						.propertyValue("PreparedStatement", pstm)//
						.propertyValue("Statement", pstm)//
						.propertyValue("Connection", obj)//
						.propertyValue("Sql", args[0])//
						.finish();
				if (pstmInterfaces == null)
				{
					pstmInterfaces = cgLibUtil.getAllInterfaces(pstm, IPrintable.class, ISqlValue.class);
				}
				return proxyFactory.createProxy(pstmInterfaces, logPstmInterceptor);
			}
			else if (Statement.class.isAssignableFrom(method.getReturnType()))
			{
				Statement stm = (Statement) proxy.invoke(connection, args);

				stm.setFetchSize(fetchSize);
				MethodInterceptor logStmInterceptor = beanContext.registerBean(LogStatementInterceptor.class)//
						.propertyValue("Statement", stm)//
						.propertyValue("Connection", obj)//
						.finish();
				if (stmInterfaces == null)
				{
					stmInterfaces = cgLibUtil.getAllInterfaces(stm, IPrintable.class);
				}
				return proxyFactory.createProxy(stmInterfaces, logStmInterceptor);
			}
			Object result = proxy.invoke(connection, args);

			if (pooledCloseMethod.equals(method) || closeMethod.equals(method))
			{
				if (eventDispatcher != null)
				{
					eventDispatcher.dispatchEvent(new ConnectionClosedEvent((Connection) obj));
				}
				connection = null;
			}
			return result;
		}
		catch (InvocationTargetException e)
		{
			throw RuntimeExceptionUtil.mask(e, method.getExceptionTypes());
		}
	}

	public Connection getConnection()
	{
		return connection;
	}
}
