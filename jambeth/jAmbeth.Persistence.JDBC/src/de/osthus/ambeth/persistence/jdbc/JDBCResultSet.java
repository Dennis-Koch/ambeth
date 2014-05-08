package de.osthus.ambeth.persistence.jdbc;

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;

import de.osthus.ambeth.exception.RuntimeExceptionUtil;
import de.osthus.ambeth.ioc.IInitializingBean;
import de.osthus.ambeth.sensor.ISensor;
import de.osthus.ambeth.sql.IResultSet;
import de.osthus.ambeth.util.IDisposable;
import de.osthus.ambeth.util.ParamChecker;

public class JDBCResultSet implements IResultSet, IInitializingBean, IDisposable
{
	public static final String SENSOR_NAME = "de.osthus.ambeth.persistence.jdbc.JDBCResultSet";

	protected ResultSet resultSet;

	protected Object[] values;

	protected String sql;

	protected ISensor sensor;

	@Override
	public void afterPropertiesSet()
	{
		ParamChecker.assertNotNull(resultSet, "resultSet");
		ParamChecker.assertNotNull(sql, "sql");
		try
		{
			ResultSetMetaData rsMetaData = resultSet.getMetaData();
			int numberOfColumns = rsMetaData.getColumnCount();
			values = new Object[numberOfColumns];
		}
		catch (SQLException e)
		{
			throw RuntimeExceptionUtil.mask(e);
		}
		ISensor sensor = this.sensor;
		if (sensor != null)
		{
			sensor.on(sql);
		}
	}

	public void setResultSet(ResultSet resultSet)
	{
		this.resultSet = resultSet;
	}

	public void setSensor(ISensor sensor)
	{
		this.sensor = sensor;
	}

	public void setSql(String sql)
	{
		this.sql = sql;
	}

	@Override
	public void dispose()
	{
		if (resultSet == null)
		{
			return;
		}
		Statement stm = null;
		try
		{
			stm = resultSet.getStatement();
		}
		catch (Throwable e)
		{
			// Intended blank
		}
		finally
		{
			JdbcUtil.close(stm, resultSet);
		}
		resultSet = null;
		ISensor sensor = this.sensor;
		if (sensor != null)
		{
			sensor.off();
		}
	}

	@Override
	public boolean moveNext()
	{
		try
		{
			ResultSet resultSet = this.resultSet;
			if (resultSet == null)
			{
				return false;
			}
			Object[] values = this.values;
			if (!resultSet.next())
			{
				for (int a = values.length; a-- > 0;)
				{
					values[a] = null;
				}
				return false;
			}
			for (int a = values.length; a-- > 0;)
			{
				values[a] = resultSet.getObject(a + 1);
			}
			return true;
		}
		catch (Throwable e)
		{
			throw RuntimeExceptionUtil.mask(e);
		}
	}

	@Override
	public Object[] getCurrent()
	{
		return values;
	}
}
