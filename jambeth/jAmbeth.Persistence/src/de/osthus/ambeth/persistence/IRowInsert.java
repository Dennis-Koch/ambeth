package de.osthus.ambeth.persistence;

import de.osthus.ambeth.util.IDisposable;

public interface IRowInsert extends IDisposable
{

	ITable getTable();

	Object getId();

	Object getVersion();

	void set(IField field, Object value);

}
