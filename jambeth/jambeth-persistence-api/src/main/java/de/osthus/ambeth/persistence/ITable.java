package de.osthus.ambeth.persistence;

import java.util.List;

import de.osthus.ambeth.collections.ILinkedMap;
import de.osthus.ambeth.collections.IList;
import de.osthus.ambeth.merge.model.IObjRef;
import de.osthus.ambeth.merge.model.IPrimitiveUpdateItem;
import de.osthus.ambeth.merge.model.IRelationUpdateItem;
import de.osthus.ambeth.util.IParamHolder;

public interface ITable
{
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
	 * Pre-processing for delete operations. Updates or deletes the links referenced by the operation.
	 * 
	 * @param id
	 *            Unique ID identifying the object in storage.
	 * @param version
	 *            Current object version.
	 */
	void preProcessDelete(Object id, Object version);

	/**
	 * Pre-processing for update operations. Updates or deletes the links referenced by the operation.
	 * 
	 * @param id
	 *            Unique ID identifying the object in storage.
	 * @param version
	 *            Current object version.
	 * @param puis
	 *            List of primitive values contained in the operation.
	 * @param ruis
	 *            List of relations referenced by the operation.
	 */
	void preProcessUpdate(Object id, Object version, List<IPrimitiveUpdateItem> puis, List<IRelationUpdateItem> ruis);

	/**
	 * Post-processing for insert and update operations. Updates the links referenced by the operation.
	 * 
	 * @param id
	 *            Unique ID identifying the object in storage.
	 * @param version
	 *            Current object version.
	 * @param puis
	 *            List of primitive values contained in the operation.
	 * @param ruis
	 *            List of relations referenced by the operation.
	 */
	void postProcessInsertAndUpdate(Object id, Object version, List<IPrimitiveUpdateItem> puis, List<IRelationUpdateItem> ruis);

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
	IVersionCursor selectVersionWhere(List<String> additionalSelectColumnList, CharSequence whereWithOrderBySql, ILinkedMap<Integer, Object> parameters);

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
	 * Getter for table name.
	 * 
	 * @return Name of this table.
	 */
	String getName();

	/**
	 * Getter for the table name which is in quotes to allow to include the value directly in a query string
	 * 
	 * @return
	 */
	String getFullqualifiedEscapedName();

	boolean isViewBased();

	/**
	 * Getter for the type of the entities persisted in this table.
	 * 
	 * @return Type of the persisted entities.
	 */
	Class<?> getEntityType();

	/**
	 * @return True if this is an archive Table, otherwise false.
	 */
	boolean isArchive();

	/**
	 * Getter for the ID field of this table.
	 * 
	 * @return Representation of the primary key of this table.
	 */
	IField getIdField();

	/**
	 * Getter for the version field of this table.
	 * 
	 * @return Representation of the object version of this table.
	 */
	IField getVersionField();

	/**
	 * Get all primitive fields which are treated as unique and represent an alternate id for the entity retrieval
	 * 
	 * @return List of all unique constrained value fields which should be treated like alternate id fields
	 */
	IField[] getAlternateIdFields();

	/**
	 * Number of alternate id fields
	 * 
	 * @return List of all unique constrained value fields which should be treated like alternate id fields
	 */
	int getAlternateIdCount();

	/**
	 * Get all indices in the array of primitive fields which are treated as an alternate id
	 * 
	 * @return array of indices where each value refers to the index in the primitive field array
	 */
	short[] getAlternateIdFieldIndicesInFields();

	/**
	 * Get id or alternate id field dependent on the specified idIndex
	 * 
	 * @param idIndex
	 * @return primary id field, if idIndex == -1, otherwise the alternate id field at this index >= 0
	 */
	IField getIdFieldByAlternateIdIndex(byte idIndex);

	IField getCreatedOnField();

	IField getCreatedByField();

	IField getUpdatedOnField();

	IField getUpdatedByField();

	/**
	 * Getter for all value fields in this table (not id, version, updated-by, foreign key...).
	 * 
	 * @return List of all value fields.
	 */
	List<IField> getPrimitiveFields();

	List<IField> getFulltextFields();

	/**
	 * Getter for all none-system fields in this table (not id, version, updated-by...).
	 * 
	 * @return List of all value fields.
	 */
	List<IField> getAllFields();

	/**
	 * Getter for all relations referencing this table.
	 * 
	 * @return List of all relations.
	 */
	List<IDirectedLink> getLinks();

	String getSequenceName();

	/**
	 * Maps a field in this table to a member of the entity persisted in this table.
	 * 
	 * @param fieldName
	 *            Name of the field to map.
	 * @param memberName
	 *            Name of the member to map.
	 * @return Mapped field.
	 */
	IField mapField(String fieldName, String memberName);

	/**
	 * Maps a field or memberName in this table as ignored.
	 * 
	 * @param fieldName
	 *            Name of the field to map.
	 * @param memberName
	 *            Name of the member to map.
	 */
	void mapIgnore(String fieldName, String memberName);

	boolean isIgnoredField(String fieldName);

	boolean isIgnoredMember(String memberName);

	/**
	 * Maps a link referencing this table to a member of the entity persisted in this table.
	 * 
	 * @param linkName
	 *            Name of the link to map.
	 * @param memberName
	 *            Name of the member to map.
	 * @return Mapped link.
	 */
	IDirectedLink mapLink(String linkName, String memberName);

	/**
	 * Getter for a field identified by its name.
	 * 
	 * @param fieldName
	 *            Name of the field to get.
	 * @return Field identified by the given field name.
	 */
	IField getFieldByName(String fieldName);

	/**
	 * Getter for a field identified by the name of the member mapped to this field.
	 * 
	 * @param memberName
	 *            Name of the identifying member.
	 * @return Field mapped to the member.
	 */
	IField getFieldByMemberName(String memberName);

	/**
	 * Getter for a field index identified by the name of the field. The index maps directly to the field instance in the list of getAllFields().
	 * 
	 * @param memberName
	 *            Name of the identifying member.
	 * @return Field index mapped to the member (correlating to the list of getAllFields())
	 */
	int getFieldIndexByName(String memberName);

	/**
	 * Getter for a field identified by the name of the property (primitive, relation, id, and version) mapped to this field.
	 * 
	 * @param propertyName
	 *            Name of the identifying property.
	 * @return Field mapped to the property.
	 */
	IField getFieldByPropertyName(String propertyName);

	/**
	 * Getter for a link identified by its name.
	 * 
	 * @param linkName
	 *            Name of the link to get.
	 * @return Link identified by the given link name.
	 */
	IDirectedLink getLinkByName(String linkName);

	/**
	 * Getter for a link identified by the name of the member mapped to this field.
	 * 
	 * @param memberName
	 *            Name of the identifying member.
	 * @return Link mapped to the member.
	 */
	IDirectedLink getLinkByMemberName(String memberName);

	/**
	 * Getter for the member name mapped to a field in this table and referenced by a link identified by the given link name.
	 * 
	 * @param linkName
	 *            Identifying link name.
	 * @return Name of the member referenced by the given link.
	 */
	String getMemberNameByLinkName(String linkName);
}
