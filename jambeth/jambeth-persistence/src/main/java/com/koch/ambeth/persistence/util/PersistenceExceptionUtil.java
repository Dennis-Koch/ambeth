package com.koch.ambeth.persistence.util;

/*-
 * #%L
 * jambeth-persistence
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
import java.sql.SQLException;

import javax.persistence.PersistenceException;

import com.koch.ambeth.ioc.annotation.Autowired;
import com.koch.ambeth.persistence.IConnectionDialect;

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
