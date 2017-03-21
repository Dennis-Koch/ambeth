package com.koch.ambeth.persistence.api;

/*-
 * #%L
 * jambeth-persistence-api
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
import java.util.Collection;
import java.util.List;

/**
 * Interface of the jAmbeth database wrapper. Used for database related methods and accessing the
 * database meta data via jAmbeth.
 *
 * @author dennis.koch
 */
public interface IDatabaseMetaData {
	Collection<Class<?>> getHandledEntities();

	/**
	 *
	 * @return Name of this database.
	 */
	String getName();

	/**
	 *
	 * @return Names of schemas in use.
	 */
	String[] getSchemaNames();

	/**
	 * @return Maximum characters this database instance supports as a symbol name
	 */
	int getMaxNameLength();

	/**
	 * Getter for alle tables in this database.
	 *
	 * @return List of all tables in this database.
	 */
	List<ITableMetaData> getTables();

	/**
	 * Getter for all defined relations in this database.
	 *
	 * @return List of all relations defined for the content of this database.
	 */
	List<ILinkMetaData> getLinks();

	/**
	 * Getter for a table identified by the persisted entity type.
	 *
	 * @param entityType Identifying type.
	 * @return Table used for persisting the given entity type.
	 */
	ITableMetaData getTableByType(Class<?> entityType);

	/**
	 * Getter for an archive table identified by the persisted entity type.
	 *
	 * @param entityType Identifying type.
	 * @return Table used for archiving the given entity type.
	 */
	ITableMetaData getArchiveTableByType(Class<?> entityType);

	/**
	 * Getter for a permission group table table identified by either an entity table or a link table.
	 *
	 * @param tableName name of either an entity table or a link table
	 * @return The permission group table
	 */
	IPermissionGroup getPermissionGroupOfTable(String tableName);

	/**
	 * Maps a table identified by name to a given entity type.
	 *
	 * @param tableName Name of the table to map.
	 * @param entityType Type to map to a table.
	 * @return Mapped table.
	 */
	ITableMetaData mapTable(String tableName, Class<?> entityType);

	/**
	 * Maps a table identified by name as archive table to a given entity type.
	 *
	 * @param tableName Name of the table to map.
	 * @param entityType Type to map to a table.
	 * @return Mapped table.
	 */
	ITableMetaData mapArchiveTable(String tableName, Class<?> entityType);

	/**
	 * Maps a table identified by name as permission group table to a given entity type.
	 *
	 * @param tableName Name of the table to map.
	 * @param entityType Type to map to a table.
	 */
	void mapPermissionGroupTable(ITableMetaData permissionGroupTable, ITableMetaData targetTable);

	/**
	 * Getter for a table identified by name.
	 *
	 * @param tableName Identifying name.
	 * @return Table of given name.
	 */
	ITableMetaData getTableByName(String tableName);

	/**
	 * Getter for a link identified by name.
	 *
	 * @param linkName Identifying name.
	 * @return Link of given name.
	 */
	ILinkMetaData getLinkByName(String linkName);

	/**
	 *
	 * @param linkSource Name of the link-defining thing (link table or foreign key constraint)
	 * @return Link defined by the given name.
	 */
	ILinkMetaData getLinkByDefiningName(String definingName);

	void addLinkByTables(ILinkMetaData link);

	/**
	 *
	 * @param table1
	 * @param table2
	 * @return Links connection the two tables.
	 */
	ILinkMetaData[] getLinksByTables(ITableMetaData table1, ITableMetaData table2);

	/**
	 * Add a single table to the database mapping
	 *
	 * @param table
	 */
	void handleTable(ITableMetaData table);

	/**
	 * Register a new table after the context was started
	 *
	 * @param connection the current connection
	 * @param fqTableName the full qualified table name
	 * @return the newly registered table metadata
	 */
	ITableMetaData registerNewTable(Connection connection, String fqTableName);
}
