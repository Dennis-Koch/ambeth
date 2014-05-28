package de.osthus.ambeth.sql;

import de.osthus.ambeth.util.IDisposable;

public interface IResultSet extends IDisposable
{
	boolean moveNext();

	Object[] getCurrent();
}
