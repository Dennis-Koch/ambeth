package de.osthus.ambeth.persistence.jdbc.connection;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.Set;

import net.sf.cglib.proxy.MethodInterceptor;
import net.sf.cglib.proxy.MethodProxy;
import de.osthus.ambeth.collections.HashSet;
import de.osthus.ambeth.config.Property;
import de.osthus.ambeth.exception.RuntimeExceptionUtil;
import de.osthus.ambeth.ioc.IInitializingBean;
import de.osthus.ambeth.log.ILogger;
import de.osthus.ambeth.log.LogInstance;
import de.osthus.ambeth.log.LogTypesUtil;
import de.osthus.ambeth.objectcollector.IThreadLocalObjectCollector;
import de.osthus.ambeth.persistence.jdbc.config.PersistenceJdbcConfigurationConstants;
import de.osthus.ambeth.persistence.parallel.IModifyingDatabase;
import de.osthus.ambeth.proxy.CascadedInterceptor;
import de.osthus.ambeth.sensor.ISensor;
import de.osthus.ambeth.sensor.Sensor;
import de.osthus.ambeth.util.IPersistenceExceptionUtil;
import de.osthus.ambeth.util.IPrintable;
import de.osthus.ambeth.util.ParamChecker;
import de.osthus.ambeth.util.ReflectUtil;
import de.osthus.ambeth.util.StringBuilderUtil;

public class LogStatementInterceptor implements MethodInterceptor, IInitializingBean, IPrintable
{
	public static final String SENSOR_NAME = "de.osthus.ambeth.persistence.jdbc.connection.LogStatementInterceptor";

	public static final Set<Method> notLoggedMethods = new HashSet<Method>(0.5f);

	public static final Method addBatchMethod;

	public static final Method getConnectionMethod;

	public static final Method executeQueryMethod;

	public static final Method executeBatchMethod;

	static
	{
		try
		{
			addBatchMethod = Statement.class.getMethod("addBatch", String.class);
			executeQueryMethod = Statement.class.getMethod("executeQuery", String.class);
			executeBatchMethod = Statement.class.getMethod("executeBatch");
			getConnectionMethod = Statement.class.getMethod("getConnection");
			notLoggedMethods.add(Statement.class.getMethod("close"));
			notLoggedMethods.add(Object.class.getDeclaredMethod("finalize"));
			notLoggedMethods.add(Object.class.getMethod("toString"));
			notLoggedMethods.add(Object.class.getMethod("equals", Object.class));
			notLoggedMethods.add(Object.class.getMethod("hashCode"));
			for (Method method : ReflectUtil.getMethods(Statement.class))
			{
				if (method.getName().startsWith("get") || method.getName().startsWith("set"))
				{
					notLoggedMethods.add(method);
				}
			}
		}
		catch (Throwable e)
		{
			throw RuntimeExceptionUtil.mask(e);
		}
	}

	@LogInstance
	private ILogger log;

	protected Statement statement;

	protected Connection connection;

	protected IModifyingDatabase modifyingDatabase;

	protected IThreadLocalObjectCollector objectCollector;

	protected IPersistenceExceptionUtil persistenceExceptionUtil;

	protected int identityHashCode;

	protected boolean isLogExceptionActive;

	protected boolean isJdbcTraceActive;

	protected int batchCount;

	protected int batchCountWithEqualSql;

	protected String recentSql;

	@Sensor(name = LogStatementInterceptor.SENSOR_NAME)
	protected ISensor sensor;

	@Override
	public void afterPropertiesSet() throws Throwable
	{
		ParamChecker.assertNotNull(connection, "Connection");
		ParamChecker.assertNotNull(objectCollector, "objectCollector");
		ParamChecker.assertNotNull(persistenceExceptionUtil, "PersistenceExceptionUtil");
		ParamChecker.assertNotNull(statement, "Statement");
		identityHashCode = System.identityHashCode(statement);
	}

	public void setConnection(Connection connection)
	{
		this.connection = connection;
	}

	public void setModifyingDatabase(IModifyingDatabase modifyingDatabase)
	{
		this.modifyingDatabase = modifyingDatabase;
	}

	public void setObjectCollector(IThreadLocalObjectCollector objectCollector)
	{
		this.objectCollector = objectCollector;
	}

	public void setPersistenceExceptionUtil(IPersistenceExceptionUtil persistenceExceptionUtil)
	{
		this.persistenceExceptionUtil = persistenceExceptionUtil;
	}

	public void setStatement(Statement statement)
	{
		this.statement = statement;
	}

	@Property(name = PersistenceJdbcConfigurationConstants.JdbcTraceActive, defaultValue = "true")
	public void setJdbcTraceActive(boolean isJdbcTraceActive)
	{
		this.isJdbcTraceActive = isJdbcTraceActive;
	}

	@Property(name = PersistenceJdbcConfigurationConstants.JdbcLogExceptionActive, defaultValue = "false")
	public void setLogExceptionActive(boolean isLogExceptionActive)
	{
		this.isLogExceptionActive = isLogExceptionActive;
	}

	protected ILogger getLog()
	{
		return log;
	}

	protected String getSqlIntern(Method method, Object[] args)
	{
		if (args.length > 0)
		{
			Object arg = args[0];
			if (arg instanceof String)
			{
				return (String) arg;
			}
		}
		return null;
	}

	@Override
	public Object intercept(Object obj, Method method, Object[] args, MethodProxy proxy) throws Throwable
	{
		if (CascadedInterceptor.finalizeMethod.equals(method))
		{
			return null;
		}
		if (getConnectionMethod.equals(method))
		{
			return connection;
		}
		try
		{
			boolean doLog = true;
			if (addBatchMethod.equals(method))
			{
				batchCount++;

				String currentSql = (String) args[0];
				if (recentSql == null || recentSql.equals(currentSql))
				{
					batchCountWithEqualSql++;
					int batchCountWithEqualSql = this.batchCountWithEqualSql;
					if (batchCountWithEqualSql > 100000)
					{
						doLog = batchCountWithEqualSql % 10000 == 0;
					}
					else if (batchCountWithEqualSql > 10000)
					{
						doLog = batchCountWithEqualSql % 1000 == 0;
					}
					else if (batchCountWithEqualSql > 1000)
					{
						doLog = batchCountWithEqualSql % 100 == 0;
					}
					else if (batchCountWithEqualSql > 100)
					{
						doLog = batchCountWithEqualSql % 10 == 0;
					}
				}
				recentSql = currentSql;
			}
			boolean doNotLog = notLoggedMethods.contains(method);
			ISensor sensor = this.sensor;
			if (sensor != null && !doNotLog)
			{
				String sql = getSqlIntern(method, args);
				if (sql == null)
				{
					sql = recentSql;
				}
				sensor.on(sql);
			}
			try
			{
				long start = System.currentTimeMillis();
				Object result = proxy.invoke(statement, args);
				if (result instanceof ResultSet)
				{
					((ResultSet) result).setFetchSize(1000);
				}
				if (doNotLog)
				{
					return result;
				}
				long end = System.currentTimeMillis();
				if (doLog)
				{
					logMeasurement(method, args, end - start);
				}
				String methodName = method.getName();
				if (modifyingDatabase != null && (methodName.equals("execute") || methodName.equals("executeUpdate")))
				{
					modifyingDatabase.setModifyingDatabase(true);
				}
				return result;
			}
			finally
			{
				if (sensor != null && !doNotLog)
				{
					sensor.off();
				}
			}
		}
		catch (InvocationTargetException e)
		{
			logError(e.getCause(), method, args);
			throw persistenceExceptionUtil.mask(e.getCause(), getSqlIntern(method, args));
		}
		catch (Throwable e)
		{
			logError(e, method, args);
			throw persistenceExceptionUtil.mask(e, getSqlIntern(method, args));
		}
		finally
		{
			if (executeBatchMethod.equals(method))
			{
				batchCount = 0;
				batchCountWithEqualSql = 0;
				recentSql = null;
			}
		}
	}

	protected void logError(Throwable e, Method method, Object[] args)
	{
		ILogger log = getLog();
		if (log.isErrorEnabled() && isLogExceptionActive)
		{
			if (executeQueryMethod.equals(method))
			{
				log.error("[" + identityHashCode + "] " + method.getName() + ": " + args[0], e);
			}
			else if (addBatchMethod.equals(method))
			{
				log.error("[" + identityHashCode + "] " + method.getName() + ": " + batchCount + ") " + args[0], e);
			}
			else if (executeBatchMethod.equals(method))
			{
				log.error("[" + identityHashCode + "] " + method.getName() + ": " + batchCount + " items", e);
			}
			else if (method.getName().startsWith("execute"))
			{
				log.error("[" + identityHashCode + "] " + method.getName() + ": " + args[0], e);
			}
			else if (isJdbcTraceActive)
			{
				log.error("[" + identityHashCode + "] " + LogTypesUtil.printMethod(method, true), e);
			}
		}
	}

	protected void logMeasurement(Method method, Object[] args, long timeSpent)
	{
		ILogger log = getLog();
		if (log.isDebugEnabled())
		{
			if (addBatchMethod.equals(method))
			{
				log.debug(StringBuilderUtil.concat(objectCollector, "[", identityHashCode, " ", timeSpent, " ms] ", method.getName(), ": ", batchCount, ") ",
						args[0]));
			}
			else if (executeBatchMethod.equals(method))
			{
				log.debug(StringBuilderUtil.concat(objectCollector, "[", identityHashCode, " ", timeSpent, " ms] ", method.getName(), ": ", batchCount,
						" items"));
			}
			else if (method.getName().startsWith("execute"))
			{
				log.debug(StringBuilderUtil.concat(objectCollector, "[", identityHashCode, " ", timeSpent, " ms] ", method.getName(), ": ", args[0]));
			}
			else if (isJdbcTraceActive)
			{
				log.debug(StringBuilderUtil.concat(objectCollector, "[", identityHashCode, " ", timeSpent, " ms] ", LogTypesUtil.printMethod(method, true)));
			}
		}
	}

	@Override
	public String toString()
	{
		StringBuilder sb = new StringBuilder();
		toString(sb);
		return sb.toString();
	}

	@Override
	public void toString(StringBuilder sb)
	{
		sb.append(getClass().getName());
	}
}
