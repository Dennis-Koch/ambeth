package de.osthus.ambeth.persistence;

import java.sql.Connection;
import java.sql.SQLException;

public interface IConfigurableDatabaseMetaData
{
	boolean isLinkArchiveTable(String tableName);

	ILinkMetaData mapLink(ILinkMetaData link);

	boolean isFieldNullable(Connection connection, IFieldMetaData field) throws SQLException;
}
