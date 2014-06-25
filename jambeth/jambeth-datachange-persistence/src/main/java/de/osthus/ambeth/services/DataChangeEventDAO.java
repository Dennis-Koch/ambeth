package de.osthus.ambeth.services;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;

import de.osthus.ambeth.annotation.NoProxy;
import de.osthus.ambeth.exception.RuntimeExceptionUtil;
import de.osthus.ambeth.ioc.IStartingBean;
import de.osthus.ambeth.ioc.annotation.Autowired;
import de.osthus.ambeth.log.ILogger;
import de.osthus.ambeth.log.LogInstance;
import de.osthus.ambeth.model.DataChangeEventBO;
import de.osthus.ambeth.persistence.jdbc.JdbcUtil;
import de.osthus.ambeth.proxy.MergeContext;
import de.osthus.ambeth.proxy.PersistenceContext;
import de.osthus.ambeth.query.IQuery;
import de.osthus.ambeth.query.IQueryBuilderFactory;

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
