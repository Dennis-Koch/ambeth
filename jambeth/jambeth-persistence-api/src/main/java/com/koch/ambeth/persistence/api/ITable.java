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

import java.util.List;

import com.koch.ambeth.query.persistence.IVersionCursor;
import com.koch.ambeth.service.merge.model.IObjRef;
import com.koch.ambeth.util.collections.ILinkedMap;

public interface ITable {
	ITableMetaData getMetaData();

	void startBatch();

	int[] finishBatch();

	void clearBatch();

	/**
	 * Persists a new object to storage.
	 *
	 * @param id Unique ID identifying the object in storage.
	 * @param puis Map of member names and primitive values.
	 * @return New version converted to the correct type.
	 */
	Object insert(Object id, ILinkedMap<IFieldMetaData, Object> puis);

	/**
	 * Updates a persisted object in storage.
	 *
	 * @param id Unique ID identifying the object in storage.
	 * @param version Current object version.
	 * @param puis Map of primitive values to update.
	 * @return New version converted to the correct type.
	 */
	Object update(Object id, Object version, ILinkedMap<IFieldMetaData, Object> puis);

	/**
	 * Deletes a list of persisted objects from storage identified by ids and versions.
	 *
	 * @param oris Object references of the objects to delete.
	 */
	void delete(List<IObjRef> oris);

	/**
	 * Truncates this table.
	 */
	void deleteAll();

	/**
	 * Selects ID and version of all entities in table.
	 *
	 * @return Cursor to the IDs and versions.
	 */
	IVersionCursor selectAll();

	/**
	 * Selects ID and version of all given entities in table.
	 *
	 * @param ids List of IDs identifying the entities to select.
	 * @return Cursor to ID and version of all selected entities.
	 */
	IVersionCursor selectVersion(List<?> ids);

	/**
	 * Selects ID and version of all given entities in table.
	 *
	 * @param alternateIdMemberName Name of the alternate ID member.
	 * @param alternateIds List of IDs identifying the entities to select.
	 * @return Cursor to ID and version of all selected entities.
	 */
	IVersionCursor selectVersion(String alternateIdMemberName, List<?> alternateIds);

	/**
	 * Selects ID and version according to the given where clause.
	 *
	 * @param whereSql SQL string containing the WHERE clause without the key word
	 * @return Cursor to ID and version of all selected entities.
	 */
	IVersionCursor selectVersionWhere(CharSequence whereSql);

	/**
	 * Selects ID and version according to the given where clause.
	 *
	 * @param additionalSelectColumnList SQL string containing additional parts which are necessary to
	 *        fulfill the order-by request It may be null
	 * @param whereWithOrderBySql SQL string containing the WHERE clause without the key word,
	 *        including order-by parts
	 * @param params
	 * @return Cursor to ID and version of all selected entities.
	 */
	IVersionCursor selectVersionWhere(List<String> additionalSelectColumnList, CharSequence whereSql,
			CharSequence orderBySql, CharSequence limitSql, List<Object> parameters);

	/**
	 * Selects ID, version, and all value fields of all given entities in table.
	 *
	 * @param ids List of IDs identifying the entities to select.
	 * @return Cursor to ID, version, and value fields of all selected entities.
	 */
	ICursor selectValues(List<?> ids);

	/**
	 * Selects ID, version, and all value fields of all given entities in table.
	 *
	 * @param alternateIdMemberName Name of alternate ID member.
	 * @param alternateIds Alternate IDs identifying the entities to select.
	 * @return Cursor to ID, version, and value fields of all selected entities.
	 */
	ICursor selectValues(String alternateIdMemberName, List<?> alternateIds);

	/**
	 * Getter for all relations referencing this table.
	 *
	 * @return List of all relations.
	 */
	List<IDirectedLink> getLinks();

	/**
	 * Getter for a link identified by its name.
	 *
	 * @param linkName Name of the link to get.
	 * @return Link identified by the given link name.
	 */
	IDirectedLink getLinkByName(String linkName);

	/**
	 * Getter for a link identified by the link containing field name on this table.
	 *
	 * @param fieldName Field name representing a link.
	 * @return Link identified by the given field name.
	 */
	IDirectedLink getLinkByFieldName(String fieldName);

	/**
	 * Getter for a link identified by the name of the member mapped to this field.
	 *
	 * @param memberName Name of the identifying member.
	 * @return Link mapped to the member.
	 */
	IDirectedLink getLinkByMemberName(String memberName);

	/**
	 * Call this method if you created new links from/to this table
	 */
	void updateLinks();
}
