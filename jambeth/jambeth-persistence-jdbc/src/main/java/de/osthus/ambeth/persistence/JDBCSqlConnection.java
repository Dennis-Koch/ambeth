package de.osthus.ambeth.persistence;

import java.sql.Array;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;
import java.util.Map.Entry;

import javax.persistence.PersistenceException;

import de.osthus.ambeth.collections.ArrayList;
import de.osthus.ambeth.collections.ILinkedMap;
import de.osthus.ambeth.collections.IList;
import de.osthus.ambeth.exception.RuntimeExceptionUtil;
import de.osthus.ambeth.log.ILogger;
import de.osthus.ambeth.log.LogInstance;
import de.osthus.ambeth.persistence.jdbc.IConnectionExtension;
import de.osthus.ambeth.persistence.jdbc.JDBCResultSet;
import de.osthus.ambeth.persistence.jdbc.JdbcUtil;
import de.osthus.ambeth.sensor.ISensor;
import de.osthus.ambeth.sensor.Sensor;
import de.osthus.ambeth.sql.IResultSet;
import de.osthus.ambeth.sql.SqlConnection;
import de.osthus.ambeth.util.ParamChecker;

public class JDBCSqlConnection extends SqlConnection
{
	@SuppressWarnings("unused")
	@LogInstance
	private ILogger log;

	protected Connection connection;

	protected IConnectionExtension connectionExtension;

	protected IDatabase database;

	@Sensor(name = JDBCResultSet.SENSOR_NAME)
	protected ISensor jdbcResultSetSensor;

	@Override
	public void afterPropertiesSet()
	{
		super.afterPropertiesSet();

		ParamChecker.assertNotNull(connection, "Connection");
		ParamChecker.assertNotNull(connectionExtension, "ConnectionExtension");
		ParamChecker.assertNotNull(database, "Database");
	}

	public void setConnection(Connection connection)
	{
		this.connection = connection;
	}

	public void setConnectionExtension(IConnectionExtension connectionExtension)
	{
		this.connectionExtension = connectionExtension;
	}

	public void setDatabase(IDatabase database)
	{
		this.database = database;
	}

	@Override
	public void directSql(String sql)
	{
		Statement stm = null;
		try
		{
			stm = connection.createStatement();
			stm.execute(sql);
		}
		catch (SQLException e)
		{
			throw RuntimeExceptionUtil.mask(e);
		}
		finally
		{
			JdbcUtil.close(stm);
		}
	}

	@Override
	protected void queueSqlExecute(String sql, ILinkedMap<Integer, Object> params)
	{
		Statement stm = null;
		try
		{
			if (params != null)
			{
				PreparedStatement pstm = connection.prepareStatement(sql);
				stm = pstm;
				for (Entry<Integer, Object> entry : params)
				{
					Integer index = entry.getKey();
					Object value = entry.getValue();
					pstm.setObject(index.intValue(), value);
				}
				pstm.execute();
			}
			else
			{
				stm = connection.createStatement();
				stm.execute(sql);
			}
		}
		catch (SQLException e)
		{
			throw RuntimeExceptionUtil.mask(e);
		}
		finally
		{
			JdbcUtil.close(stm);
		}
	}

	@Override
	protected int[] queueSqlExecute(String[] sql)
	{
		Statement stm = null;
		try
		{
			stm = connection.createStatement();
			for (int i = sql.length; i-- > 0;)
			{
				stm.addBatch(sql[i]);
			}
			return stm.executeBatch();
		}
		catch (SQLException e)
		{
			throw RuntimeExceptionUtil.mask(e);
		}
		finally
		{
			JdbcUtil.close(stm);
		}
	}

	@Override
	protected IResultSet sqlSelect(String sql, ILinkedMap<Integer, Object> params)
	{
		ResultSet resultSet = null;
		Statement stm = null;
		boolean success = false;
		try
		{
			if (params != null)
			{
				IList<Object> arraysToDispose = null;
				IConnectionExtension connectionExtension = this.connectionExtension;
				try
				{
					PreparedStatement pstm = connection.prepareStatement(sql);
					stm = pstm;
					for (Entry<Integer, Object> entry : params)
					{
						Integer index = entry.getKey();
						Object value = entry.getValue();
						if (value instanceof ArrayQueryItem)
						{
							ArrayQueryItem aqi = (ArrayQueryItem) value;
							value = connectionExtension.createJDBCArray(aqi.getFieldType(), aqi.getValues());

							if (arraysToDispose == null)
							{
								arraysToDispose = new ArrayList<Object>();
							}
							arraysToDispose.add(value);
						}
						pstm.setObject(index.intValue(), value);
					}
					resultSet = pstm.executeQuery();
				}
				finally
				{
					if (arraysToDispose != null)
					{
						for (int a = arraysToDispose.size(); a-- > 0;)
						{
							disposeArray(arraysToDispose.get(a));
						}
					}
				}
			}
			else
			{
				stm = connection.createStatement();

				resultSet = stm.executeQuery(sql);
			}
			success = true;
		}
		catch (PersistenceException e)
		{
			throw e;
		}
		catch (Throwable e)
		{
			throw RuntimeExceptionUtil.mask(e, "Error occured while executing sql: " + sql);
		}
		finally
		{
			if (!success)
			{
				JdbcUtil.close(stm, resultSet);
			}
		}
		JDBCResultSet jdbcResultSet = new JDBCResultSet();
		jdbcResultSet.setResultSet(resultSet);
		jdbcResultSet.setSensor(jdbcResultSetSensor);
		jdbcResultSet.setSql(sql);
		jdbcResultSet.afterPropertiesSet();
		return jdbcResultSet;
	}

	@Override
	protected Object createArray(String tableName, String idFieldName, List<?> ids)
	{
		Class<?> fieldType = null;
		ITable table = database.getTableByName(tableName);
		if (table != null)
		{
			fieldType = table.getFieldByName(idFieldName).getFieldType();
		}
		else
		{
			ILink link = database.getLinkByName(tableName);
			if (link.getFromField().getName().equals(idFieldName))
			{
				fieldType = link.getFromField().getFieldType();
			}
			else if (link.getToField().getName().equals(idFieldName))
			{
				fieldType = link.getToField().getFieldType();
			}
		}
		if (fieldType == null)
		{
			throw new IllegalStateException("Must never happen");
		}
		return connectionExtension.createJDBCArray(fieldType, ids.toArray());
	}

	@Override
	protected void disposeArray(Object array)
	{
		if (array == null)
		{
			return;
		}
		try
		{
			((Array) array).free();
		}
		catch (SQLException e)
		{
			// Intended blank
		}
	}

}
