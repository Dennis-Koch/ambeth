package com.koch.ambeth.persistence.h2;

/*-
 * #%L
 * jambeth-persistence-h2
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
import com.koch.ambeth.log.ILogger;
import com.koch.ambeth.log.LogInstance;
import com.koch.ambeth.persistence.api.ITableMetaData;
import com.koch.ambeth.persistence.jdbc.JdbcUtil;
import com.koch.ambeth.persistence.sql.AbstractCachingPrimaryKeyProvider;
import com.koch.ambeth.util.StringBuilderUtil;
import com.koch.ambeth.util.exception.RuntimeExceptionUtil;
import com.koch.ambeth.util.objectcollector.IObjectCollector;

public class H2SequencePrimaryKeyProvider extends AbstractCachingPrimaryKeyProvider {
	@SuppressWarnings("unused")
	@LogInstance
	private ILogger log;

	@Autowired
	protected Connection connection;

	@Autowired
	protected IObjectCollector objectCollector;

	@Override
	protected void acquireIdsIntern(ITableMetaData table, int count, List<Object> targetIdList) {
		String sql = StringBuilderUtil.concat(objectCollector.getCurrent(), "SELECT ",
				table.getSequenceName(), ".nextval FROM DUAL");
		PreparedStatement pstm = null;
		ResultSet rs = null;
		try {
			pstm = connection.prepareStatement(sql);
			while (count-- > 0) {
				rs = pstm.executeQuery();
				while (rs.next()) {
					Object id = rs.getObject(1); // We have only 1 column in the select so it is ok to
																				// retrieve it by the unique id
					targetIdList.add(id);
				}
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
