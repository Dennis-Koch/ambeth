package com.koch.ambeth.persistence.jdbc.connection;

import java.lang.ref.Reference;
import java.lang.ref.WeakReference;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Wrapper;

import javax.sql.PooledConnection;

import com.koch.ambeth.event.IEventDispatcher;
import com.koch.ambeth.ioc.IServiceContext;
import com.koch.ambeth.ioc.annotation.Autowired;
import com.koch.ambeth.ioc.config.Property;
import com.koch.ambeth.ioc.proxy.ICgLibUtil;
import com.koch.ambeth.log.ILogger;
import com.koch.ambeth.log.LogInstance;
import com.koch.ambeth.persistence.SQLState;
import com.koch.ambeth.persistence.api.database.ITransactionInfo;
import com.koch.ambeth.persistence.config.PersistenceConfigurationConstants;
import com.koch.ambeth.persistence.jdbc.connection.IConnectionKeyHandle;
import com.koch.ambeth.persistence.jdbc.connection.IPreparedConnectionHolder;
import com.koch.ambeth.persistence.jdbc.event.ConnectionClosedEvent;
import com.koch.ambeth.service.log.interceptor.LogInterceptor;
import com.koch.ambeth.util.IPrintable;
import com.koch.ambeth.util.collections.IdentityWeakSmartCopyMap;
import com.koch.ambeth.util.exception.RuntimeExceptionUtil;
import com.koch.ambeth.util.proxy.AbstractSimpleInterceptor;
import com.koch.ambeth.util.proxy.IProxyFactory;

import net.sf.cglib.proxy.MethodInterceptor;
import net.sf.cglib.proxy.MethodProxy;

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
	protected IServiceContext beanContext;

	@Autowired
	protected ICgLibUtil cgLibUtil;

	@Autowired
	protected Connection connection;

	@Autowired(optional = true)
	protected IEventDispatcher eventDispatcher;

	@Autowired
	protected IProxyFactory proxyFactory;

	@Autowired(optional = true)
	protected ITransactionInfo transactionInfo;

	protected boolean preparedConnection;

	@Property(name = PersistenceConfigurationConstants.FetchSize, defaultValue = "100")
	protected int fetchSize;

	protected final IConnectionKeyHandle connectionKeyHandle;

	protected Class<?>[] pstmInterfaces;

	protected Class<?>[] stmInterfaces;

	protected final IdentityWeakSmartCopyMap<Object, Reference<Object>> unwrappedObjectToProxyMap = new IdentityWeakSmartCopyMap<Object, Reference<Object>>();

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
					log.debug("[cn:" + System.identityHashCode(connection) + " tx:" + getSessionId() + "] closed connection");
				}
				if (eventDispatcher != null)
				{
					eventDispatcher.dispatchEvent(new ConnectionClosedEvent((Connection) obj));
				}
				connection = null;
			}
			else if (unwrapMethod.equals(method))
			{
				Reference<Object> proxyOfResultR = unwrappedObjectToProxyMap.get(result);
				Object proxyOfResult = proxyOfResultR != null ? proxyOfResultR.get() : null;
				if (proxyOfResult == null)
				{
					LogInterceptor logInterceptor = beanContext.registerBean(LogInterceptor.class)//
							.propertyValue("Target", result)//
							.finish();
					proxyOfResult = proxyFactory.createProxy(cgLibUtil.getAllInterfaces(result), logInterceptor);
					unwrappedObjectToProxyMap.put(result, new WeakReference<Object>(proxyOfResult));
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

	protected String getSessionId()
	{
		if (transactionInfo == null)
		{
			return "-";
		}
		return Long.toString(transactionInfo.getSessionId());
	}

	public Connection getConnection()
	{
		return connection;
	}
}
