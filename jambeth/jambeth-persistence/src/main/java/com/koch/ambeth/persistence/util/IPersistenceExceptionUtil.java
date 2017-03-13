package com.koch.ambeth.persistence.util;

import javax.persistence.PersistenceException;

public interface IPersistenceExceptionUtil
{
	PersistenceException mask(Throwable e);

	PersistenceException mask(Throwable e, String relatedSql);
}