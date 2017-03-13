package com.koch.ambeth.persistence;

import java.sql.Connection;
import java.sql.SQLException;

import com.koch.ambeth.persistence.api.IFieldMetaData;
import com.koch.ambeth.persistence.api.ILinkMetaData;

public interface IConfigurableDatabaseMetaData
{
	boolean isLinkArchiveTable(String tableName);

	ILinkMetaData mapLink(ILinkMetaData link);

	boolean isFieldNullable(Connection connection, IFieldMetaData field) throws SQLException;
}
