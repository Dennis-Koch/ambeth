package de.osthus.ambeth.persistence;

import java.util.List;

import de.osthus.ambeth.collections.ILinkedMap;
import de.osthus.ambeth.collections.IList;
import de.osthus.ambeth.merge.model.IObjRef;
import de.osthus.ambeth.util.IParamHolder;

public interface ITable
{
	ITableMetaData getMetaData();

	void startBatch();

	int[] finishBatch();

	void clearBatch();

	/**
	 * Persists a new object to storage.
	 * 
	 * @param id
	 *            Unique ID identifying the object in storage.
	 * @param newId
	 *            Id converted to the correct type. Outgoing parameter.
	 * @param puis
	 *            Map of member names and primitive values.
	 * @return New version converted to the correct type.
	 */
	Object insert(Object id, IParamHolder<Object> newId, ILinkedMap<String, Object> puis);

	/**
	 * Updates a persisted object in storage.
	 * 
	 * @param id
	 *            Unique ID identifying the object in storage.
	 * @param version
	 *            Current object version.
	 * @param puis
	 *            Map of primitive values to update.
	 * @return New version converted to the correct type.
	 */
	Object update(Object id, Object version, ILinkedMap<String, Object> puis);

	/**
	 * Deletes a list of persisted objects from storage identified by ids and versions.
	 * 
	 * @param oris
	 *            Object references of the objects to delete.
	 */
	void delete(List<IObjRef> oris);

	/**
	 * Truncates this table.
	 */
	void deleteAll();

	/**
	 * Acquires a list of new and unique IDs for this table.
	 * 
	 * @param count
	 *            Number of IDs to fetch.
	 * @return List of new IDs.
	 */
	IList<Object> acquireIds(int count);

	/**
	 * Selects ID and version of all entities in table.
	 * 
	 * @return Cursor to the IDs and versions.
	 */
	IVersionCursor selectAll();

	/**
	 * Selects ID and version of all given entities in table.
	 * 
	 * @param ids
	 *            List of IDs identifying the entities to select.
	 * @return Cursor to ID and version of all selected entities.
	 */
	IVersionCursor selectVersion(List<?> ids);

	/**
	 * Selects ID and version of all given entities in table.
	 * 
	 * @param alternateIdMemberName
	 *            Name of the alternate ID member.
	 * @param alternateIds
	 *            List of IDs identifying the entities to select.
	 * @return Cursor to ID and version of all selected entities.
	 */
	IVersionCursor selectVersion(String alternateIdMemberName, List<?> alternateIds);

	/**
	 * Selects ID and version according to the given where clause.
	 * 
	 * @param whereSql
	 *            SQL string containing the WHERE clause without the key word
	 * @return Cursor to ID and version of all selected entities.
	 */
	IVersionCursor selectVersionWhere(CharSequence whereSql);

	/**
	 * Selects ID and version according to the given where clause.
	 * 
	 * @param additionalSelectColumnList
	 *            SQL string containing additional parts which are necessary to fulfill the order-by request It may be null
	 * @param whereWithOrderBySql
	 *            SQL string containing the WHERE clause without the key word, including order-by parts
	 * @param params
	 * @return Cursor to ID and version of all selected entities.
	 */
	IVersionCursor selectVersionWhere(List<String> additionalSelectColumnList, CharSequence whereSql, CharSequence orderBySql, CharSequence limitSql,
			List<Object> parameters);

	/**
	 * Selects ID, version, and all value fields of all given entities in table.
	 * 
	 * @param ids
	 *            List of IDs identifying the entities to select.
	 * @return Cursor to ID, version, and value fields of all selected entities.
	 */
	ICursor selectValues(List<?> ids);

	/**
	 * Selects ID, version, and all value fields of all given entities in table.
	 * 
	 * @param alternateIdMemberName
	 *            Name of alternate ID member.
	 * @param alternateIds
	 *            Alternate IDs identifying the entities to select.
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
	 * @param linkName
	 *            Name of the link to get.
	 * @return Link identified by the given link name.
	 */
	IDirectedLink getLinkByName(String linkName);

	/**
	 * Getter for a link identified by the link containing field name on this table.
	 * 
	 * @param fieldName
	 *            Field name representing a link.
	 * @return Link identified by the given field name.
	 */
	IDirectedLink getLinkByFieldName(String fieldName);

	/**
	 * Getter for a link identified by the name of the member mapped to this field.
	 * 
	 * @param memberName
	 *            Name of the identifying member.
	 * @return Link mapped to the member.
	 */
	IDirectedLink getLinkByMemberName(String memberName);
}
