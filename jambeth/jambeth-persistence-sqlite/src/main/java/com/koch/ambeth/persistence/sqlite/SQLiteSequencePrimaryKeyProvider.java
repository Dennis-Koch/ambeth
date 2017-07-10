package com.koch.ambeth.persistence.sqlite;

/*-
 * #%L
 * jambeth-persistence-sqlite
 * %%
 * Copyright (C) 2017 Koch Softwaredevelopment
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

     http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
 * #L%
 */

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.List;

import com.koch.ambeth.ioc.annotation.Autowired;
import com.koch.ambeth.persistence.IConnectionDialect;
import com.koch.ambeth.persistence.api.ITableMetaData;
import com.koch.ambeth.persistence.jdbc.JdbcUtil;
import com.koch.ambeth.persistence.orm.XmlDatabaseMapper;
import com.koch.ambeth.persistence.sql.AbstractCachingPrimaryKeyProvider;
import com.koch.ambeth.util.exception.RuntimeExceptionUtil;

public class SQLiteSequencePrimaryKeyProvider extends AbstractCachingPrimaryKeyProvider {
	@Autowired
	protected Connection connection;

	@Autowired
	protected IConnectionDialect connectionDialect;

	@Override
	protected void acquireIdsIntern(ITableMetaData table, int count, List<Object> targetIdList) {
		String[] schemaAndName = XmlDatabaseMapper.splitSchemaAndName(table.getSequenceName());
		if (schemaAndName[0] == null) {
			// if no schema is explicitly specified in the sequence we look in the schema of the table
			schemaAndName[0] = XmlDatabaseMapper
					.splitSchemaAndName(table.getFullqualifiedEscapedName())[0];
		}
		PreparedStatement pstm = null;
		ResultSet rs = null;
		try {
			pstm = connection.prepareStatement("SELECT nextval('"
					+ connectionDialect.escapeSchemaAndSymbolName(schemaAndName[0], schemaAndName[1])
					+ "') FROM generate_series(1,?)");
			pstm.setInt(1, count);
			rs = pstm.executeQuery();
			while (rs.next()) {
				Object id = rs.getObject(1); // We have only 1 column in the select so it is ok to retrieve
																			// it by the unique id
				targetIdList.add(id);
			}
		}
		catch (Exception e) {
			throw RuntimeExceptionUtil.mask(e);
		}
		finally {
			JdbcUtil.close(pstm, rs);
		}
	}
}
