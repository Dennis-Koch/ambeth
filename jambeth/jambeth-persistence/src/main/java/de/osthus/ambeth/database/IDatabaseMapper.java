package de.osthus.ambeth.database;

import java.sql.Connection;

import de.osthus.ambeth.persistence.IDatabaseMetaData;

public interface IDatabaseMapper
{
	void mapFields(Connection connection, IDatabaseMetaData database);

	void mapLinks(Connection connection, IDatabaseMetaData database);
}
