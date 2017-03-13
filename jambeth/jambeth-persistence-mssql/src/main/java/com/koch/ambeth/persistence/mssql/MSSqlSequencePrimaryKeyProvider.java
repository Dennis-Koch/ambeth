package com.koch.ambeth.persistence.mssql;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.List;

import com.koch.ambeth.ioc.annotation.Autowired;
import com.koch.ambeth.log.ILogger;
import com.koch.ambeth.log.LogInstance;
import com.koch.ambeth.persistence.api.ITableMetaData;
import com.koch.ambeth.persistence.jdbc.JdbcUtil;
import com.koch.ambeth.persistence.sql.AbstractCachingPrimaryKeyProvider;
import com.koch.ambeth.util.StringBuilderUtil;
import com.koch.ambeth.util.exception.RuntimeExceptionUtil;
import com.koch.ambeth.util.objectcollector.IObjectCollector;

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
