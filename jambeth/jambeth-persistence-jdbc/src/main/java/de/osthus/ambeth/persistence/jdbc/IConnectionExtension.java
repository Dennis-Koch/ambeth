package de.osthus.ambeth.persistence.jdbc;

import java.sql.Array;

public interface IConnectionExtension
{
	Array createJDBCArray(Class<?> expectedComponentType, Object javaArray);
}
