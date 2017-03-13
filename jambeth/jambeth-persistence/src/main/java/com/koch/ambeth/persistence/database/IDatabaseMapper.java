package com.koch.ambeth.persistence.database;

import java.sql.Connection;

import com.koch.ambeth.persistence.api.IDatabaseMetaData;

public interface IDatabaseMapper
{
	void mapFields(Connection connection, String[] schemaNames, IDatabaseMetaData database);

	void mapLinks(Connection connection, String[] schemaNames, IDatabaseMetaData database);
}
