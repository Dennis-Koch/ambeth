package com.koch.ambeth.datachange.persistence.services;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;

import com.koch.ambeth.datachange.persistence.model.DataChangeEventBO;
import com.koch.ambeth.ioc.IStartingBean;
import com.koch.ambeth.ioc.annotation.Autowired;
import com.koch.ambeth.log.ILogger;
import com.koch.ambeth.log.LogInstance;
import com.koch.ambeth.merge.proxy.MergeContext;
import com.koch.ambeth.merge.proxy.PersistenceContext;
import com.koch.ambeth.persistence.jdbc.JdbcUtil;
import com.koch.ambeth.query.IQuery;
import com.koch.ambeth.query.IQueryBuilderFactory;
import com.koch.ambeth.util.annotation.NoProxy;
import com.koch.ambeth.util.exception.RuntimeExceptionUtil;

@PersistenceContext
@MergeContext
public class DataChangeEventDAO implements IDataChangeEventDAO, IStartingBean
{
	@SuppressWarnings("unused")
	@LogInstance
	private ILogger log;

	@Autowired
	protected Connection connection;

	@Autowired
	protected IQueryBuilderFactory qbf;

	protected IQuery<DataChangeEventBO> retrieveAll;

	@Override
	public void afterStarted() throws Throwable
	{
		retrieveAll = qbf.create(DataChangeEventBO.class).build();
	}

	@Override
	public void save(DataChangeEventBO dataChangeEvent)
	{
		throw new UnsupportedOperationException();
	}

	@Override
	public List<DataChangeEventBO> retrieveAll()
	{
		return retrieveAll.retrieve();
	}

	@Override
	@NoProxy
	public void removeBefore(long time)
	{
		Statement stmt = null;
		try
		{
			stmt = connection.createStatement();
			stmt.execute("DELETE FROM DATA_CHANGE_ENTRY WHERE INSERT_PARENT IN (SELECT ID FROM DATA_CHANGE_EVENT WHERE CHANGE_TIME < " + time
					+ ") OR UPDATE_PARENT IN (SELECT ID FROM DATA_CHANGE_EVENT WHERE CHANGE_TIME < " + time
					+ ") OR DELETE_PARENT IN (SELECT ID FROM DATA_CHANGE_EVENT WHERE CHANGE_TIME < " + time + ")");
			stmt.execute("DELETE FROM DATA_CHANGE_EVENT WHERE CHANGE_TIME < " + time);
		}
		catch (SQLException e)
		{
			throw RuntimeExceptionUtil.mask(e);
		}
		finally
		{
			JdbcUtil.close(stmt);
		}
	}
}
