package com.koch.ambeth.persistence.jdbc;

import java.sql.Savepoint;

import com.koch.ambeth.persistence.api.ISavepoint;

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
