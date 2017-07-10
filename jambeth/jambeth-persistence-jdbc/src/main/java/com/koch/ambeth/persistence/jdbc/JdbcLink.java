package com.koch.ambeth.persistence.jdbc;

/*-
 * #%L
 * jambeth-persistence-jdbc
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
import java.sql.SQLException;
import java.util.List;
import java.util.Map.Entry;

import com.koch.ambeth.ioc.annotation.Autowired;
import com.koch.ambeth.persistence.sql.SqlLink;
import com.koch.ambeth.util.appendable.AppendableStringBuilder;
import com.koch.ambeth.util.collections.ILinkedMap;
import com.koch.ambeth.util.collections.LinkedHashMap;
import com.koch.ambeth.util.exception.RuntimeExceptionUtil;
import com.koch.ambeth.util.objectcollector.IThreadLocalObjectCollector;

public class JdbcLink extends SqlLink {
	@Autowired
	protected IConnectionExtension connectionExtension;

	@Autowired
	protected Connection connection;

	protected ILinkedMap<String, PreparedStatement> namesToPstmMap;

	@Override
	public void startBatch() {
		if (namesToPstmMap != null) {
			throw new IllegalStateException("Must never happen");
		}
		namesToPstmMap = new LinkedHashMap<>();
		super.startBatch();
	}

	@Override
	public int[] finishBatch() {
		for (Entry<String, PreparedStatement> entry : namesToPstmMap) {
			PreparedStatement pstm = entry.getValue();
			try {
				pstm.executeBatch();
			}
			catch (Exception e) {
				throw RuntimeExceptionUtil.mask(e);
			}
		}
		return super.finishBatch();
	}

	@Override
	public void clearBatch() {
		for (Entry<String, PreparedStatement> entry : namesToPstmMap) {
			PreparedStatement pstm = entry.getValue();
			try {
				pstm.close();
			}
			catch (SQLException e) {
				// Intended blank
			}
		}
		namesToPstmMap = null;
		super.clearBatch();
	}

	@Override
	protected void linkIdsIntern(String names, Object fromId, Class<?> toIdType, List<Object> toIds) {
		try {
			PreparedStatement pstm = namesToPstmMap.get(names);
			if (pstm == null) {
				IThreadLocalObjectCollector tlObjectCollector = objectCollector.getCurrent();
				AppendableStringBuilder sb = tlObjectCollector.create(AppendableStringBuilder.class);
				try {
					sb.append("INSERT INTO ");
					sqlBuilder.appendName(getMetaData().getName(), sb);
					sb.append(" (").append(names).append(") VALUES (?,?)");
					pstm = connection.prepareStatement(sb.toString());
					namesToPstmMap.put(names, pstm);
				}
				finally {
					tlObjectCollector.dispose(sb);
				}
			}
			pstm.setObject(1, fromId);
			for (int a = 0, size = toIds.size(); a < size; a++) {
				pstm.setObject(2, toIds.get(a));
				pstm.addBatch();
			}
			pstm.clearParameters();
		}
		catch (SQLException e) {
			throw RuntimeExceptionUtil.mask(e);
		}
	}

	@Override
	protected void unlinkIdsIntern(String whereSQL, Class<?> toIdType, List<Object> parameters) {
		IThreadLocalObjectCollector tlObjectCollector = objectCollector.getCurrent();
		try {
			PreparedStatement pstm = namesToPstmMap.get(whereSQL);
			if (pstm == null) {
				AppendableStringBuilder sb = tlObjectCollector.create(AppendableStringBuilder.class);
				try {
					sb.append("DELETE FROM ");
					sqlBuilder.appendName(getMetaData().getName(), sb);
					sb.append(" WHERE ").append(whereSQL);

					pstm = connection.prepareStatement(sb.toString());
					namesToPstmMap.put(whereSQL, pstm);
				}
				finally {
					tlObjectCollector.dispose(sb);
				}
			}
			for (int index = 0, size = parameters.size(); index < size; index++) {
				pstm.setObject(index + 1, parameters.get(index));
			}
			pstm.addBatch();
			pstm.clearParameters();
		}
		catch (SQLException e) {
			throw RuntimeExceptionUtil.mask(e);
		}
	}
}
