package de.osthus.ambeth.util;

import java.lang.reflect.InvocationTargetException;
import java.sql.SQLException;

import javax.persistence.PersistenceException;

import de.osthus.ambeth.ioc.annotation.Autowired;
import de.osthus.ambeth.persistence.IConnectionDialect;

public class PersistenceExceptionUtil implements IPersistenceExceptionUtil
{
	@Autowired
	protected IConnectionDialect connectionDialect;

	@Override
	public PersistenceException mask(Throwable e)
	{
		return mask(e, null);
	}

	@Override
	public PersistenceException mask(Throwable e, String relatedSql)
	{
		while (e instanceof InvocationTargetException)
		{
			e = ((InvocationTargetException) e).getTargetException();
		}
		if (e instanceof PersistenceException)
		{
			return (PersistenceException) e;
		}
		if (e instanceof SQLException)
		{
			PersistenceException pException = connectionDialect.createPersistenceException((SQLException) e, relatedSql);

			if (pException != null)
			{
				return pException;
			}
		}
		PersistenceException pe = new PersistenceException(relatedSql, e);
		pe.setStackTrace(new StackTraceElement[0]);
		return pe;
	}
}
