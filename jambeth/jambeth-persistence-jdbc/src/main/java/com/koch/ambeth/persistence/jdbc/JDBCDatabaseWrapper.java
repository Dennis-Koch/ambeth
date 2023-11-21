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
import java.sql.SQLException;
import java.util.Map.Entry;

import com.koch.ambeth.ioc.annotation.Autowired;
import com.koch.ambeth.log.ILogger;
import com.koch.ambeth.log.LogInstance;
import com.koch.ambeth.persistence.Database;
import com.koch.ambeth.persistence.DirectedLink;
import com.koch.ambeth.persistence.IConnectionDialect;
import com.koch.ambeth.persistence.api.IDirectedLink;
import com.koch.ambeth.persistence.api.IDirectedLinkMetaData;
import com.koch.ambeth.persistence.api.ILink;
import com.koch.ambeth.persistence.api.ILinkMetaData;
import com.koch.ambeth.persistence.api.ISavepoint;
import com.koch.ambeth.persistence.api.ITable;
import com.koch.ambeth.persistence.api.ITableMetaData;
import com.koch.ambeth.persistence.util.IAlreadyLinkedCache;
import com.koch.ambeth.util.collections.IdentityHashMap;
import com.koch.ambeth.util.exception.RuntimeExceptionUtil;
import com.koch.ambeth.util.state.IStateRollback;
import lombok.SneakyThrows;

public class JDBCDatabaseWrapper extends Database {
	@LogInstance
	private ILogger log;

	@Autowired
	protected Connection connection;

	@Autowired
	protected IAlreadyLinkedCache alreadyLinkedCache;

	@Autowired
	protected IConnectionDialect connectionDialect;

	protected long lastTestTime = System.currentTimeMillis(), trustTime = 10000;

	protected IdentityHashMap<ITableMetaData, ITable> alreadyCreatedTableMap;

	private IdentityHashMap<ILinkMetaData, ILink> alreadyCreatedLinkMap;

	private IdentityHashMap<IDirectedLinkMetaData, IDirectedLink> alreadyCreatedDirectedLinkMap;

	@Override
	public void afterPropertiesSet() throws Throwable {
		super.afterPropertiesSet();

		alreadyCreatedLinkMap = new IdentityHashMap<>();
		alreadyCreatedDirectedLinkMap = new IdentityHashMap<>();
		alreadyCreatedTableMap = new IdentityHashMap<>();

		for (ITableMetaData tableMD : metaData.getTables()) {
			JdbcTable table = new JdbcTable();
			tables.add(table);
			alreadyCreatedTableMap.put(tableMD, table);
		}
		for (ILinkMetaData linkMD : metaData.getLinks()) {
			JdbcLink link = new JdbcLink();
			links.add(link);
			alreadyCreatedLinkMap.put(linkMD, link);

			alreadyCreatedDirectedLinkMap.put(linkMD.getDirectedLink(), new DirectedLink());
			alreadyCreatedDirectedLinkMap.put(linkMD.getReverseDirectedLink(), new DirectedLink());
		}
		for (Entry<ITableMetaData, ITable> entry : alreadyCreatedTableMap) {
			ITableMetaData tableMD = entry.getKey();
			JdbcTable table = (JdbcTable) entry.getValue();

			table.init(tableMD, alreadyCreatedDirectedLinkMap);

			table = serviceContext.registerWithLifecycle(table)//
					.propertyValue("MetaData", tableMD)//
					.finish();
		}
		for (Entry<IDirectedLinkMetaData, IDirectedLink> entry : alreadyCreatedDirectedLinkMap) {
			IDirectedLinkMetaData directedLinkMD = entry.getKey();
			DirectedLink directedLink = (DirectedLink) entry.getValue();

			directedLink = serviceContext.registerWithLifecycle(directedLink)//
					.propertyValue("MetaData", directedLinkMD)//
					.propertyValue("FromTable",
							getExistingValue(alreadyCreatedTableMap, directedLinkMD.getFromTable()))//
					.propertyValue("ToTable",
							getExistingValue(alreadyCreatedTableMap, directedLinkMD.getToTable()))//
					.propertyValue("Link", getExistingValue(alreadyCreatedLinkMap, directedLinkMD.getLink()))//
					.propertyValue("Reverse",
							getExistingValue(alreadyCreatedDirectedLinkMap, directedLinkMD.getReverseLink()))//
					.finish();
		}
		for (Entry<ILinkMetaData, ILink> entry : alreadyCreatedLinkMap) {
			ILinkMetaData linkMD = entry.getKey();
			JdbcLink link = (JdbcLink) entry.getValue();

			link = serviceContext.registerWithLifecycle(link)//
					.propertyValue("MetaData", linkMD)//
					.propertyValue("FromTable",
							getExistingValue(alreadyCreatedTableMap, linkMD.getFromTable()))//
					.propertyValue("ToTable", getExistingValue(alreadyCreatedTableMap, linkMD.getToTable()))//
					.propertyValue("DirectedLink",
							getExistingValue(alreadyCreatedDirectedLinkMap, linkMD.getDirectedLink()))//
					.propertyValue("ReverseDirectedLink",
							getExistingValue(alreadyCreatedDirectedLinkMap, linkMD.getReverseDirectedLink()))//
					.finish();
		}
		for (ITable table : tables) {
			Class<?> entityType = table.getMetaData().getEntityType();
			nameToTableDict.put(table.getMetaData().getName(), table);

			if (entityType == null) {
				continue;
			}
			if (table.getMetaData().isArchive()) {
				typeToArchiveTableDict.put(entityType, table);
			}
			else {
				typeToTableDict.put(entityType, table);
			}
		}
	}

	@Override
	public void destroy() throws Throwable {
		connection = null;
		super.destroy();
	}

	protected <K, V> V getExistingValue(IdentityHashMap<K, V> map, K key) {
		if (key == null) {
			return null;
		}
		V value = map.get(key);
		if (value != null) {
			return value;
		}
		throw new IllegalStateException("No value for key: " + key);
	}

	@Override
	public void flush() {
		try {
			connectionDialect.commit(connection);
		}
		catch (Exception e) {
			if (e instanceof SQLException) {
				throw connectionDialect.createPersistenceException((SQLException) e, null);
			}
			throw e;
		}
	}

	@Override
	public void revert() {
		alreadyLinkedCache.clear();
		try {
			connectionDialect.rollback(connection);
		}
		catch (Exception e) {
			if (e instanceof SQLException) {
				throw connectionDialect.createPersistenceException((SQLException) e, null);
			}
			throw e;
		}
	}

	@Override
	public void revert(ISavepoint savepoint) {
		alreadyLinkedCache.clear();
		try {
			rollback(savepoint);
		}
		catch (Exception e) {
			if (e instanceof SQLException) {
				throw connectionDialect.createPersistenceException((SQLException) e, null);
			}
			throw e;
		}
	}

	@Override
	public boolean test() {
		if (System.currentTimeMillis() - lastTestTime <= trustTime) {
			return true;
		}
		try {
			try {
				return connection.isValid(0);
			}
			catch (AbstractMethodError e) {
				// Oracle driver does not support this operation
				return !connection.isClosed();
			}
		}
		catch (SQLException e) {
			return false;
		}
		finally {
			lastTestTime = System.currentTimeMillis();
		}
	}

	@Override
	public ISavepoint setSavepoint() {
		try {
			return new JdbcSavepoint(connection.setSavepoint());
		}
		catch (SQLException e) {
			throw connectionDialect.createPersistenceException(e, null);
		}
	}

	@Override
	public void releaseSavepoint(ISavepoint savepoint) {
		try {
			connectionDialect.releaseSavepoint(((JdbcSavepoint) savepoint).getSavepoint(), connection);
		}
		catch (Exception e) {
			if (e instanceof SQLException) {
				throw connectionDialect.createPersistenceException((SQLException) e, null);
			}
			throw e;
		}
	}

	@Override
	public void rollback(ISavepoint savepoint) {
		try {
			connection.rollback(((JdbcSavepoint) savepoint).getSavepoint());
		}
		catch (SQLException e) {
			throw connectionDialect.createPersistenceException(e, null);
		}
	}

	@Override
	public IStateRollback disableConstraints() {
		return connectionDialect.disableConstraints(connection);
	}

	@Override
	public void registerNewTable(String tableName) {
		ITableMetaData tableMD = ((JDBCDatabaseMetaData) metaData).getTableByName(tableName);
		String fqTableName = tableMD.getName();
		JdbcTable table = new JdbcTable();
		tables.add(table);

		IdentityHashMap<ILinkMetaData, ILink> newlyCreatedLinkMap =
				new IdentityHashMap<>();
		IdentityHashMap<IDirectedLinkMetaData, IDirectedLink> newlyCreatedDirectedLinkMap =
				new IdentityHashMap<>();
		for (ILinkMetaData linkMD : metaData.getLinks()) {
			JdbcLink link = new JdbcLink();
			if (alreadyCreatedLinkMap.containsKey(linkMD)) {
				continue;
			}
			links.add(link);
			newlyCreatedLinkMap.put(linkMD, link);

			newlyCreatedDirectedLinkMap.put(linkMD.getDirectedLink(), new DirectedLink());
			newlyCreatedDirectedLinkMap.put(linkMD.getReverseDirectedLink(), new DirectedLink());
		}
		table.init(tableMD, newlyCreatedDirectedLinkMap);
		table = serviceContext.registerWithLifecycle(table)//
				.propertyValue("MetaData", tableMD)//
				.finish();
		alreadyCreatedTableMap.put(tableMD, table);

		for (Entry<IDirectedLinkMetaData, IDirectedLink> entry : newlyCreatedDirectedLinkMap) {
			IDirectedLinkMetaData directedLinkMD = entry.getKey();
			if (directedLinkMD.getFromTable().getName().equals(fqTableName)
					|| directedLinkMD.getToTable().getName().equals(fqTableName)) {
				DirectedLink directedLink = (DirectedLink) entry.getValue();
				directedLink = serviceContext.registerWithLifecycle(directedLink)//
						.propertyValue("MetaData", directedLinkMD)//
						.propertyValue("FromTable",
								getExistingValue(alreadyCreatedTableMap, directedLinkMD.getFromTable()))//
						.propertyValue("ToTable",
								getExistingValue(alreadyCreatedTableMap, directedLinkMD.getToTable()))//
						.propertyValue("Link", getExistingValue(newlyCreatedLinkMap, directedLinkMD.getLink()))//
						.propertyValue("Reverse",
								getExistingValue(newlyCreatedDirectedLinkMap, directedLinkMD.getReverseLink()))//
						.finish();
			}
		}
		for (Entry<ILinkMetaData, ILink> entry : newlyCreatedLinkMap) {
			ILinkMetaData linkMD = entry.getKey();
			if (linkMD.getFromTable().getName().equals(fqTableName)
					|| linkMD.getToTable().getName().equals(fqTableName)) {
				JdbcLink link = (JdbcLink) entry.getValue();
				link = serviceContext.registerWithLifecycle(link)//
						.propertyValue("MetaData", linkMD)//
						.propertyValue("FromTable",
								getExistingValue(alreadyCreatedTableMap, linkMD.getFromTable()))//
						.propertyValue("ToTable", getExistingValue(alreadyCreatedTableMap, linkMD.getToTable()))//
						.propertyValue("DirectedLink",
								getExistingValue(newlyCreatedDirectedLinkMap, linkMD.getDirectedLink()))//
						.propertyValue("ReverseDirectedLink",
								getExistingValue(newlyCreatedDirectedLinkMap, linkMD.getReverseDirectedLink()))//
						.finish();
			}
		}

		Class<?> entityType = table.getMetaData().getEntityType();
		nameToTableDict.put(table.getMetaData().getName(), table);

		if (table.getMetaData().isArchive()) {
			typeToArchiveTableDict.put(entityType, table);
		}
		else {
			typeToTableDict.put(entityType, table);
		}
	}
}
