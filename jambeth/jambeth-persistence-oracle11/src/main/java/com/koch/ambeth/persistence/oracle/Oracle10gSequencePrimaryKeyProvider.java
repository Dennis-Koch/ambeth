package com.koch.ambeth.persistence.oracle;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.List;

import com.koch.ambeth.ioc.annotation.Autowired;
import com.koch.ambeth.log.ILogger;
import com.koch.ambeth.log.LogInstance;
import com.koch.ambeth.persistence.IConnectionDialect;
import com.koch.ambeth.persistence.api.ITableMetaData;
import com.koch.ambeth.persistence.jdbc.JdbcUtil;
import com.koch.ambeth.persistence.orm.XmlDatabaseMapper;
import com.koch.ambeth.persistence.sql.AbstractCachingPrimaryKeyProvider;
import com.koch.ambeth.util.exception.RuntimeExceptionUtil;

public class Oracle10gSequencePrimaryKeyProvider extends AbstractCachingPrimaryKeyProvider
{
	@SuppressWarnings("unused")
	@LogInstance
	private ILogger log;

	@Autowired
	protected Connection connection;

	@Autowired
	protected IConnectionDialect connectionDialect;

	@Override
	protected void acquireIdsIntern(ITableMetaData table, int count, List<Object> targetIdList)
	{
		String[] schemaAndName = XmlDatabaseMapper.splitSchemaAndName(table.getSequenceName());
		if (schemaAndName[0] == null)
		{
			// if no schema is explicitly specified in the sequence we look in the schema of the table
			schemaAndName[0] = XmlDatabaseMapper.splitSchemaAndName(table.getFullqualifiedEscapedName())[0];
		}
		PreparedStatement pstm = null;
		ResultSet rs = null;
		try
		{
			pstm = connection.prepareStatement("SELECT " + connectionDialect.escapeSchemaAndSymbolName(schemaAndName[0], schemaAndName[1])
					+ ".nextval FROM DUAL CONNECT BY level<=?");
			pstm.setInt(1, count);
			rs = pstm.executeQuery();
			while (rs.next())
			{
				Object id = rs.getObject(1); // We have only 1 column in the select so it is ok to retrieve it by the unique id
				targetIdList.add(id);
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
