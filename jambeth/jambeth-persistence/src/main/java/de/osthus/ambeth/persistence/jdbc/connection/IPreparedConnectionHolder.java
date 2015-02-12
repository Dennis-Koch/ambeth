package de.osthus.ambeth.persistence.jdbc.connection;

public interface IPreparedConnectionHolder
{
	boolean isPreparedConnection();

	void setPreparedConnection(boolean preparedConnection);
}