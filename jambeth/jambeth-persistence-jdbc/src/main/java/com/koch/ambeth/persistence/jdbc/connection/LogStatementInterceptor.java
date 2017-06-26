package com.koch.ambeth.persistence.jdbc.connection;

/*-
 * #%L
 * jambeth-persistence-jdbc
 * %%
 * Copyright (C) 2017 Koch Softwaredevelopment
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

     http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
 * #L%
 */

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.Set;

import com.koch.ambeth.ioc.IInitializingBean;
import com.koch.ambeth.ioc.annotation.Autowired;
import com.koch.ambeth.ioc.config.Property;
import com.koch.ambeth.log.ILogger;
import com.koch.ambeth.log.LogInstance;
import com.koch.ambeth.log.LogTypesUtil;
import com.koch.ambeth.persistence.api.database.ITransactionInfo;
import com.koch.ambeth.persistence.jdbc.config.PersistenceJdbcConfigurationConstants;
import com.koch.ambeth.persistence.parallel.IModifyingDatabase;
import com.koch.ambeth.persistence.util.IPersistenceExceptionUtil;
import com.koch.ambeth.util.IPrintable;
import com.koch.ambeth.util.ParamChecker;
import com.koch.ambeth.util.ReflectUtil;
import com.koch.ambeth.util.StringBuilderUtil;
import com.koch.ambeth.util.collections.HashSet;
import com.koch.ambeth.util.exception.RuntimeExceptionUtil;
import com.koch.ambeth.util.objectcollector.IThreadLocalObjectCollector;
import com.koch.ambeth.util.proxy.AbstractSimpleInterceptor;
import com.koch.ambeth.util.sensor.ISensor;
import com.koch.ambeth.util.sensor.Sensor;

import net.sf.cglib.proxy.MethodProxy;

public class LogStatementInterceptor extends AbstractSimpleInterceptor
		implements IInitializingBean, IPrintable {
	public static final String SENSOR_NAME = "com.koch.ambeth.persistence.jdbc.connection.LogStatementInterceptor";

	public static final Set<Method> notLoggedMethods = new HashSet<>(0.5f);

	public static final Method addBatchMethod;

	public static final Method getConnectionMethod;

	public static final Method executeQueryMethod;

	public static final Method executeBatchMethod;

	static {
		try {
			addBatchMethod = Statement.class.getMethod("addBatch", String.class);
			executeQueryMethod = Statement.class.getMethod("executeQuery", String.class);
			executeBatchMethod = Statement.class.getMethod("executeBatch");
			getConnectionMethod = Statement.class.getMethod("getConnection");
			notLoggedMethods.add(Statement.class.getMethod("close"));
			notLoggedMethods.add(Object.class.getDeclaredMethod("finalize"));
			notLoggedMethods.add(Object.class.getMethod("toString"));
			notLoggedMethods.add(Object.class.getMethod("equals", Object.class));
			notLoggedMethods.add(Object.class.getMethod("hashCode"));
			for (Method method : ReflectUtil.getMethods(Statement.class)) {
				if (method.getName().startsWith("get") || method.getName().startsWith("set")) {
					notLoggedMethods.add(method);
				}
			}
		}
		catch (Exception e) {
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

	@Autowired(optional = true)
	protected ITransactionInfo transactionInfo;

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
	public void afterPropertiesSet() throws Throwable {
		ParamChecker.assertNotNull(statement, "Statement");
		identityHashCode = System.identityHashCode(statement.getConnection());
	}

	protected ILogger getLog() {
		return log;
	}

	protected String getSqlIntern(Method method, Object[] args) {
		if (args.length > 0) {
			Object arg = args[0];
			if (arg instanceof String) {
				return (String) arg;
			}
		}
		return null;
	}

	@Override
	protected Object interceptIntern(Object obj, Method method, Object[] args, MethodProxy proxy)
			throws Throwable {
		if (getConnectionMethod.equals(method)) {
			return connection;
		}
		try {
			boolean doLog = true;
			if (addBatchMethod.equals(method)) {
				batchCount++;

				String currentSql = (String) args[0];
				if (recentSql == null || recentSql.equals(currentSql)) {
					batchCountWithEqualSql++;
					int batchCountWithEqualSql = this.batchCountWithEqualSql;
					if (batchCountWithEqualSql > 100000) {
						doLog = batchCountWithEqualSql % 10000 == 0;
					}
					else if (batchCountWithEqualSql > 10000) {
						doLog = batchCountWithEqualSql % 1000 == 0;
					}
					else if (batchCountWithEqualSql > 1000) {
						doLog = batchCountWithEqualSql % 100 == 0;
					}
					else if (batchCountWithEqualSql > 100) {
						doLog = batchCountWithEqualSql % 10 == 0;
					}
				}
				recentSql = currentSql;
			}
			boolean doNotLog = notLoggedMethods.contains(method);
			ISensor sensor = this.sensor;
			if (sensor != null && !doNotLog) {
				String sql = getSqlIntern(method, args);
				if (sql == null) {
					sql = recentSql;
				}
				if (sql == null) {
					sensor.on();
				}
				else {
					sensor.on(sql);
				}
			}
			try {
				long start = System.currentTimeMillis();
				Object result = proxy.invoke(statement, args);
				if (result instanceof ResultSet) {
					((ResultSet) result).setFetchSize(1000);
				}
				if (doNotLog) {
					return result;
				}
				long end = System.currentTimeMillis();
				if (doLog) {
					logMeasurement(method, args, end - start);
				}
				String methodName = method.getName();
				if (modifyingDatabase != null
						&& ("execute".equals(methodName) || "executeUpdate".equals(methodName))) {
					modifyingDatabase.setModifyingDatabase(true);
				}
				return result;
			}
			finally {
				if (sensor != null && !doNotLog) {
					sensor.off();
				}
			}
		}
		catch (InvocationTargetException e) {
			logError(e.getCause(), method, args);
			throw persistenceExceptionUtil.mask(e.getCause(), getSqlIntern(method, args));
		}
		catch (Throwable e) {
			logError(e, method, args);
			throw persistenceExceptionUtil.mask(e, getSqlIntern(method, args));
		}
		finally {
			if (executeBatchMethod.equals(method)) {
				batchCount = 0;
				batchCountWithEqualSql = 0;
				recentSql = null;
			}
		}
	}

	protected void logError(Throwable e, Method method, Object[] args) {
		ILogger log = getLog();
		if (log.isErrorEnabled() && isLogExceptionActive) {
			if (executeQueryMethod.equals(method)) {
				log.error("[cn:" + identityHashCode + " tx:" + getSessionId() + "] " + method.getName()
						+ ": " + args[0], e);
			}
			else if (addBatchMethod.equals(method)) {
				log.error("[cn:" + identityHashCode + " tx:" + getSessionId() + "] " + method.getName()
						+ ": " + batchCount + ") " + args[0], e);
			}
			else if (executeBatchMethod.equals(method)) {
				log.error("[cn:" + identityHashCode + " tx:" + getSessionId() + "] " + method.getName()
						+ ": " + batchCount + " items", e);
			}
			else if (method.getName().startsWith("execute")) {
				log.error("[cn:" + identityHashCode + " tx:" + getSessionId() + "] " + method.getName()
						+ ": " + args[0], e);
			}
			else if (isJdbcTraceActive) {
				log.error("[cn:" + identityHashCode + " tx:" + getSessionId() + "] "
						+ LogTypesUtil.printMethod(method, true), e);
			}
		}
	}

	protected void logMeasurement(Method method, Object[] args, long timeSpent) {
		ILogger log = getLog();
		if (log.isDebugEnabled()) {
			if (addBatchMethod.equals(method)) {
				log.debug(StringBuilderUtil.concat(objectCollector, "[cn:", identityHashCode, " tx:",
						getSessionId(), " ", timeSpent, " ms] ", method.getName(), ": ", batchCount, ") ",
						args[0]));
			}
			else if (executeBatchMethod.equals(method)) {
				log.debug(StringBuilderUtil.concat(objectCollector, "[cn:", identityHashCode, " tx:",
						getSessionId(), " ", timeSpent, " ms] ", method.getName(), ": ", batchCount, " items"));
			}
			else if (method.getName().startsWith("execute")) {
				log.debug(StringBuilderUtil.concat(objectCollector, "[cn:", identityHashCode, " tx:",
						getSessionId(), " ", timeSpent, " ms] ", method.getName(), ": ", args[0]));
			}
			else if (isJdbcTraceActive) {
				log.debug(StringBuilderUtil.concat(objectCollector, "[cn:", identityHashCode, " tx:",
						getSessionId(), " ", timeSpent, " ms] ", LogTypesUtil.printMethod(method, true)));
			}
		}
	}

	protected String getSessionId() {
		if (transactionInfo == null) {
			return "-";
		}
		return Long.toString(transactionInfo.getSessionId());
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		toString(sb);
		return sb.toString();
	}

	@Override
	public void toString(StringBuilder sb) {
		sb.append(getClass().getName());
	}
}
