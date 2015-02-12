package de.osthus.ambeth.persistence.jdbc.connection;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.Set;

import net.sf.cglib.proxy.MethodProxy;
import de.osthus.ambeth.collections.HashSet;
import de.osthus.ambeth.config.Property;
import de.osthus.ambeth.exception.RuntimeExceptionUtil;
import de.osthus.ambeth.ioc.IInitializingBean;
import de.osthus.ambeth.ioc.annotation.Autowired;
import de.osthus.ambeth.log.ILogger;
import de.osthus.ambeth.log.LogInstance;
import de.osthus.ambeth.log.LogTypesUtil;
import de.osthus.ambeth.objectcollector.IThreadLocalObjectCollector;
import de.osthus.ambeth.persistence.jdbc.config.PersistenceJdbcConfigurationConstants;
import de.osthus.ambeth.persistence.parallel.IModifyingDatabase;
import de.osthus.ambeth.proxy.AbstractSimpleInterceptor;
import de.osthus.ambeth.sensor.ISensor;
import de.osthus.ambeth.sensor.Sensor;
import de.osthus.ambeth.util.IPersistenceExceptionUtil;
import de.osthus.ambeth.util.IPrintable;
import de.osthus.ambeth.util.ParamChecker;
import de.osthus.ambeth.util.ReflectUtil;
import de.osthus.ambeth.util.StringBuilderUtil;

public class LogStatementInterceptor extends AbstractSimpleInterceptor implements IInitializingBean, IPrintable
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

	@Autowired
	protected Statement statement;

	@Autowired
	protected Connection connection;

	@Autowired(optional = true)
	protected IModifyingDatabase modifyingDatabase;

	@Autowired
	protected IThreadLocalObjectCollector objectCollector;

	@Autowired
	protected IPersistenceExceptionUtil persistenceExceptionUtil;

	protected int identityHashCode;

	@Property(name = PersistenceJdbcConfigurationConstants.JdbcLogExceptionActive, defaultValue = "false")
	protected boolean isLogExceptionActive;

	@Property(name = PersistenceJdbcConfigurationConstants.JdbcTraceActive, defaultValue = "true")
	protected boolean isJdbcTraceActive;

	protected int batchCount;

	protected int batchCountWithEqualSql;

	protected String recentSql;

	@Sensor(name = LogStatementInterceptor.SENSOR_NAME)
	protected ISensor sensor;

	@Override
	public void afterPropertiesSet() throws Throwable
	{
		ParamChecker.assertNotNull(statement, "Statement");
		identityHashCode = System.identityHashCode(statement.getConnection());
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
	protected Object interceptIntern(Object obj, Method method, Object[] args, MethodProxy proxy) throws Throwable
	{
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
