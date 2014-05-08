package de.osthus.ambeth.persistence;

import java.sql.SQLException;

public interface IConfigurableDatabase
{
	boolean isLinkArchiveTable(String tableName);

	ILink mapLink(ILink link);

	boolean isFieldNullable(IField field) throws SQLException;
}
