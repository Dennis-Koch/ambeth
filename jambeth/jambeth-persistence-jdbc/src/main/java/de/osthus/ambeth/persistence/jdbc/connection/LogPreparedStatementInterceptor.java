package de.osthus.ambeth.persistence.jdbc.connection;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.sql.PreparedStatement;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import net.sf.cglib.proxy.MethodProxy;
import de.osthus.ambeth.collections.HashSet;
import de.osthus.ambeth.exception.RuntimeExceptionUtil;
import de.osthus.ambeth.ioc.annotation.Autowired;
import de.osthus.ambeth.log.ILogger;
import de.osthus.ambeth.log.LogInstance;
import de.osthus.ambeth.objectcollector.IThreadLocalObjectCollector;
import de.osthus.ambeth.proxy.CascadedInterceptor;
import de.osthus.ambeth.util.ParamChecker;
import de.osthus.ambeth.util.ReflectUtil;
import de.osthus.ambeth.util.StringBuilderUtil;

public class LogPreparedStatementInterceptor extends LogStatementInterceptor implements ISqlValue
{
	public static final Method executeUpdateMethod, executeQueryMethod;

	public static final Method executeMethod;

	public static final Method addBatchMethod;

	public static final HashSet<Method> setIndexMethods = new HashSet<Method>(0.5f);

	// Important to load the foreign static field to this static field on startup because of potential unnecessary classloading issues on finalize()
	private static final Method finalizeMethod = CascadedInterceptor.finalizeMethod;

	static
	{
		try
		{
			addBatchMethod = PreparedStatement.class.getMethod("addBatch");
			executeUpdateMethod = PreparedStatement.class.getMethod("executeUpdate");
			executeQueryMethod = PreparedStatement.class.getMethod("executeQuery");
			executeMethod = PreparedStatement.class.getMethod("execute");
			notLoggedMethods.add(PreparedStatement.class.getMethod("setObject", int.class, Object.class));
			notLoggedMethods.add(PreparedStatement.class.getMethod("clearParameters"));
			for (Method method : ReflectUtil.getMethods(PreparedStatement.class))
			{
				if (method.getName().startsWith("get") || method.getName().startsWith("set"))
				{
					notLoggedMethods.add(method);
				}
				Class<?>[] parameterTypes = method.getParameterTypes();
				if (parameterTypes.length != 2 || !int.class.equals(parameterTypes[0]) || !method.getName().startsWith("set"))
				{
					continue;
				}
				setIndexMethods.add(method);
			}
		}
		catch (Throwable e)
		{
			throw RuntimeExceptionUtil.mask(e);
		}
	}

	@LogInstance
	private ILogger log;

	protected PreparedStatement preparedStatement;

	protected String sql;

	@Autowired(optional = true)
	protected IPreparedStatementParamLogger paramLogger; // optional for debugging

	@Override
	public void afterPropertiesSet() throws Throwable
	{
		super.afterPropertiesSet();

		ParamChecker.assertNotNull(preparedStatement, "preparedStatement");
		ParamChecker.assertNotNull(sql, "sql");
	}

	public void setPreparedStatement(PreparedStatement preparedStatement)
	{
		this.preparedStatement = preparedStatement;
	}

	public void setSql(String sql)
	{
		this.sql = sql;
	}

	@Override
	protected ILogger getLog()
	{
		return log;
	}

	@Override
	public Object intercept(Object obj, Method method, Object[] args, MethodProxy proxy) throws Throwable
	{
		if (finalizeMethod.equals(method))
		{
			return null;
		}
		if (ISqlValue.class.equals(method.getDeclaringClass()))
		{
			try
			{
				return proxy.invoke(this, args);
			}
			catch (InvocationTargetException e)
			{
				throw RuntimeExceptionUtil.mask(e, method.getExceptionTypes());
			}
		}
		if (addBatchMethod.equals(method))
		{
			batchCount++;
		}
		if (paramLogger != null)
		{
			if (addBatchMethod.equals(method))
			{
				paramLogger.addBatch();
			}
			else if (paramLogger.isCallToBeLogged(method))
			{
				paramLogger.logParams(method, args);
			}
		}
		try
		{
			return super.intercept(obj, method, args, proxy);
		}
		finally
		{
			if (executeMethod.equals(method))
			{
				batchCount = 0;
			}
		}
	}

	@Override
	protected void logMeasurement(Method method, Object[] args, long timeSpent)
	{
		ILogger log = getLog();
		if (log.isDebugEnabled())
		{
			if (addBatchMethod.equals(method))
			{
				return;
			}
			else if (executeBatchMethod.equals(method))
			{
				log.debug(StringBuilderUtil.concat(objectCollector, "[", identityHashCode, " ", timeSpent, " ms] ", method.getName(), ": ", batchCount,
						" times ", getSql()));
				if (paramLogger != null)
				{
					paramLogger.doLogBatch();
				}
				return;
			}
			else if (executeMethod.equals(method) || executeUpdateMethod.equals(method) || executeQueryMethod.equals(method))
			{
				log.debug(StringBuilderUtil.concat(objectCollector, "[", identityHashCode, " ", timeSpent, " ms] ", method.getName(), ": ",
						getSqlIntern(method, args)));
				if (paramLogger != null)
				{
					paramLogger.doLog();
				}
				return;
			}
		}
		super.logMeasurement(method, args, timeSpent);
	}

	@Override
	public String getSql()
	{
		return sql;
	}

	@Override
	protected String getSqlIntern(Method method, Object[] args)
	{
		if (!setIndexMethods.contains(method))
		{
			return getSql();
		}
		String sql = getSql();
		int index = ((Integer) args[0]).intValue();
		IThreadLocalObjectCollector tlObjectCollector = objectCollector.getCurrent();
		StringBuilder sb = tlObjectCollector.create(StringBuilder.class);
		try
		{
			sb.append("((?:[^\\?]*\\?){");
			sb.append(index - 1); // PSTM param indices are 1-based
			sb.append("}[^\\?]*)(\\?)(.*)");
			Pattern pattern = Pattern.compile(sb.toString());

			Matcher matcher = pattern.matcher(sql);
			if (!matcher.matches())
			{
				return getSql();
			}
			sb.setLength(0);
			sb.append(matcher.group(1));
			sb.append(">>>?<<<");
			sb.append(matcher.group(3));
			return sb.toString();
		}
		finally
		{
			tlObjectCollector.dispose(sb);
		}
	}
}
