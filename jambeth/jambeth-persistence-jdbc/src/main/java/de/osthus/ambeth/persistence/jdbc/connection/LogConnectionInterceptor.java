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
import de.osthus.ambeth.collections.IdentityWeakSmartCopyMap;
import de.osthus.ambeth.config.Property;
import de.osthus.ambeth.event.IEventDispatcher;
import de.osthus.ambeth.exception.RuntimeExceptionUtil;
import de.osthus.ambeth.ioc.IServiceContext;
import de.osthus.ambeth.ioc.annotation.Autowired;
import de.osthus.ambeth.log.ILogger;
import de.osthus.ambeth.log.LogInstance;
import de.osthus.ambeth.log.interceptor.LogInterceptor;
import de.osthus.ambeth.persistence.SQLState;
import de.osthus.ambeth.persistence.config.PersistenceConfigurationConstants;
import de.osthus.ambeth.persistence.jdbc.event.ConnectionClosedEvent;
import de.osthus.ambeth.proxy.AbstractSimpleInterceptor;
import de.osthus.ambeth.proxy.ICgLibUtil;
import de.osthus.ambeth.proxy.IProxyFactory;
import de.osthus.ambeth.util.IPrintable;

public class LogConnectionInterceptor extends AbstractSimpleInterceptor implements IPreparedConnectionHolder
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

	protected boolean preparedConnection;

	@Property(name = PersistenceConfigurationConstants.FetchSize, defaultValue = "100")
	protected int fetchSize;

	protected final IConnectionKeyHandle connectionKeyHandle;

	protected Class<?>[] pstmInterfaces;

	protected Class<?>[] stmInterfaces;

	protected final IdentityWeakSmartCopyMap<Object, Object> unwrappedObjectToProxyMap = new IdentityWeakSmartCopyMap<Object, Object>();

	public LogConnectionInterceptor(IConnectionKeyHandle connectionKeyHandle)
	{
		this.connectionKeyHandle = connectionKeyHandle;
		unwrappedObjectToProxyMap.setAutoCleanupNullValue(true);
	}

	public void setConnection(Connection connection)
	{
		this.connection = connection;
	}

	@Override
	public boolean isPreparedConnection()
	{
		return preparedConnection;
	}

	@Override
	public void setPreparedConnection(boolean preparedConnection)
	{
		this.preparedConnection = preparedConnection;
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
		if (isWrapperForMethod.equals(method))
		{
			if (IConnectionKeyHandle.class.equals(args[0]) || IPreparedConnectionHolder.class.equals(args[0]))
			{
				return Boolean.TRUE;
			}
		}
		else if (unwrapMethod.equals(method))
		{
			if (IConnectionKeyHandle.class.equals(args[0]))
			{
				return connectionKeyHandle;
			}
			if (IPreparedConnectionHolder.class.equals(args[0]))
			{
				return this;
			}
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
				return proxyFactory.createProxy(PreparedStatement.class, pstmInterfaces, logPstmInterceptor);
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
				return proxyFactory.createProxy(Statement.class, stmInterfaces, logStmInterceptor);
			}
			Object result = proxy.invoke(connection, args);

			if (pooledCloseMethod.equals(method) || closeMethod.equals(method))
			{
				if (log.isDebugEnabled())
				{
					log.debug("[" + System.identityHashCode(connection) + "] closed connection");
				}
				if (eventDispatcher != null)
				{
					eventDispatcher.dispatchEvent(new ConnectionClosedEvent((Connection) obj));
				}
				connection = null;
			}
			else if (unwrapMethod.equals(method))
			{
				Object proxyOfResult = unwrappedObjectToProxyMap.get(result);
				if (proxyOfResult == null)
				{
					LogInterceptor logInterceptor = beanContext.registerBean(LogInterceptor.class)//
							.propertyValue("Target", result)//
							.finish();
					proxyOfResult = proxyFactory.createProxy(cgLibUtil.getAllInterfaces(result), logInterceptor);
					unwrappedObjectToProxyMap.put(result, proxyOfResult);
				}
				result = proxyOfResult;
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
