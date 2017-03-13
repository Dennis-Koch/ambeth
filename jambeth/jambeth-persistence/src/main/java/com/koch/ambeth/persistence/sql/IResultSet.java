package com.koch.ambeth.persistence.sql;

import com.koch.ambeth.util.IDisposable;

public interface IResultSet extends IDisposable
{
	boolean moveNext();

	Object[] getCurrent();
}
