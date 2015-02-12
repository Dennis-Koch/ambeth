package de.osthus.ambeth.persistence;

import java.sql.Array;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;

import javax.persistence.PersistenceException;

import de.osthus.ambeth.collections.ArrayList;
import de.osthus.ambeth.collections.IList;
import de.osthus.ambeth.exception.RuntimeExceptionUtil;
import de.osthus.ambeth.ioc.annotation.Autowired;
import de.osthus.ambeth.log.ILogger;
import de.osthus.ambeth.log.LogInstance;
import de.osthus.ambeth.persistence.jdbc.IConnectionExtension;
import de.osthus.ambeth.persistence.jdbc.JDBCResultSet;
import de.osthus.ambeth.persistence.jdbc.JdbcUtil;
import de.osthus.ambeth.sensor.ISensor;
import de.osthus.ambeth.sensor.Sensor;
import de.osthus.ambeth.sql.IResultSet;
import de.osthus.ambeth.sql.SqlConnection;

public class JDBCSqlConnection extends SqlConnection
{
	@SuppressWarnings("unused")
	@LogInstance
	private ILogger log;

	@Autowired
	protected Connection connection;

	@Autowired
	protected IConnectionExtension connectionExtension;

	@Autowired
	protected IDatabaseMetaData databaseMetaData;

	@Sensor(name = JDBCResultSet.SENSOR_NAME)
	protected ISensor jdbcResultSetSensor;

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
	protected void queueSqlExecute(String sql, List<Object> parameters)
	{
		Statement stm = null;
		try
		{
			if (parameters != null)
			{
				PreparedStatement pstm = connection.prepareStatement(sql);
				stm = pstm;
				for (int index = 0, size = parameters.size(); index < size; index++)
				{
					Object value = parameters.get(index);
					pstm.setObject(index + 1, value);
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
	protected IResultSet sqlSelect(String sql, List<Object> parameters)
	{
		ResultSet resultSet = null;
		Statement stm = null;
		boolean success = false;
		try
		{
			if (parameters != null)
			{
				IList<Object> arraysToDispose = null;
				IConnectionExtension connectionExtension = this.connectionExtension;
				try
				{
					PreparedStatement pstm = connection.prepareStatement(sql);
					stm = pstm;
					for (int index = 0, size = parameters.size(); index < size; index++)
					{
						Object value = parameters.get(index);
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
						pstm.setObject(index + 1, value);
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
		ITableMetaData table = databaseMetaData.getTableByName(tableName);
		if (table != null)
		{
			fieldType = table.getFieldByName(idFieldName).getFieldType();
		}
		else
		{
			ILinkMetaData link = databaseMetaData.getLinkByName(tableName);
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
