package com.koch.ambeth.persistence.jdbc.connection;

import java.lang.reflect.Method;

public interface IPreparedStatementParamLogger
{
	boolean isCallToBeLogged(Method method);

	void logParams(Method method, Object[] args);

	void addBatch();

	void doLog();

	void doLogBatch();
}
