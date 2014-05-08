package de.osthus.ambeth.persistence;

import java.io.InputStream;
import java.io.Reader;
import java.sql.Blob;
import java.sql.Clob;
import java.sql.Connection;

public interface ILobHandler
{
	Blob insertBlob(Connection connection, InputStream is) throws Throwable;

	Blob insertBlob(Connection connection, byte[] content) throws Throwable;

	Clob insertClob(Connection connection, Reader reader) throws Throwable;

	Clob insertClob(Connection connection, String content) throws Throwable;

	<T> T readLob(Class<T> expectedType, Object dbValue) throws Throwable;
}
