package de.osthus.ambeth.mssql;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.List;

import de.osthus.ambeth.exception.RuntimeExceptionUtil;
import de.osthus.ambeth.ioc.annotation.Autowired;
import de.osthus.ambeth.log.ILogger;
import de.osthus.ambeth.log.LogInstance;
import de.osthus.ambeth.objectcollector.IObjectCollector;
import de.osthus.ambeth.persistence.ITableMetaData;
import de.osthus.ambeth.persistence.jdbc.JdbcUtil;
import de.osthus.ambeth.sql.AbstractCachingPrimaryKeyProvider;
import de.osthus.ambeth.util.StringBuilderUtil;

public class MSSqlSequencePrimaryKeyProvider extends AbstractCachingPrimaryKeyProvider
{
	@SuppressWarnings("unused")
	@LogInstance
	private ILogger log;

	@Autowired
	protected Connection connection;

	@Autowired
	protected IObjectCollector objectCollector;

	@Override
	protected void acquireIdsIntern(ITableMetaData table, int count, List<Object> targetIdList)
	{
		String sql = StringBuilderUtil.concat(objectCollector.getCurrent(), "SELECT ", table.getSequenceName(), ".nextval FROM DUAL");
		PreparedStatement pstm = null;
		ResultSet rs = null;
		try
		{
			pstm = connection.prepareStatement(sql);
			while (count-- > 0)
			{
				rs = pstm.executeQuery();
				while (rs.next())
				{
					Object id = rs.getObject(1); // We have only 1 column in the select so it is ok to retrieve it by the unique id
					targetIdList.add(id);
				}
			}
		}
		catch (Throwable e)
		{
			throw RuntimeExceptionUtil.mask(e);
		}
		finally
		{
			JdbcUtil.close(pstm, rs);
		}
	}
}
