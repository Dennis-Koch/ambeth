package de.osthus.ambeth.persistence.jdbc;

import java.sql.Savepoint;

import de.osthus.ambeth.persistence.ISavepoint;

public class JdbcSavepoint implements ISavepoint
{
	private final Savepoint savepoint;

	public JdbcSavepoint(Savepoint savepoint)
	{
		this.savepoint = savepoint;
	}

	public Savepoint getSavepoint()
	{
		return savepoint;
	}
}
