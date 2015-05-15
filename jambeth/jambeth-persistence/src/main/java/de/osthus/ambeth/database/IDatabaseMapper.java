package de.osthus.ambeth.database;

import java.sql.Connection;

import de.osthus.ambeth.persistence.IDatabaseMetaData;

public interface IDatabaseMapper
{
	void mapFields(Connection connection, String[] schemaNames, IDatabaseMetaData database);

	void mapLinks(Connection connection, String[] schemaNames, IDatabaseMetaData database);
}
