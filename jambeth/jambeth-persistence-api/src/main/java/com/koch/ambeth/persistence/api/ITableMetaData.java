package com.koch.ambeth.persistence.api;

import java.util.List;

public interface ITableMetaData
{
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
	 * @return True if this is an archive table, otherwise false.
	 */
	boolean isArchive();

	/**
	 * @return True if this is an permission group table, otherwise false.
	 */
	boolean isPermissionGroup();

	/**
	 * Getter for the ID field of this table.
	 * 
	 * @return Representation of the primary key of this table.
	 */
	IFieldMetaData getIdField();

	/**
	 * Getter for the ID field of this table.
	 * 
	 * @return Representation of the primary key of this table.
	 */
	IFieldMetaData[] getIdFields();

	/**
	 * Getter for the version field of this table.
	 * 
	 * @return Representation of the object version of this table.
	 */
	IFieldMetaData getVersionField();

	/**
	 * Getter for the descriminator field of this table.
	 * 
	 * @return Representation of the descriminator of this table.
	 */
	IFieldMetaData getDescriminatorField();

	/**
	 * Get all primitive fields which are treated as unique and represent an alternate id for the entity retrieval
	 * 
	 * @return List of all unique constrained value fields which should be treated like alternate id fields
	 */
	IFieldMetaData[] getAlternateIdFields();

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
	IFieldMetaData getIdFieldByAlternateIdIndex(int idIndex);

	IFieldMetaData getCreatedOnField();

	IFieldMetaData getCreatedByField();

	IFieldMetaData getUpdatedOnField();

	IFieldMetaData getUpdatedByField();

	/**
	 * Getter for all value fields in this table (not id, version, updated-by, foreign key...).
	 * 
	 * @return List of all value fields.
	 */
	List<IFieldMetaData> getPrimitiveFields();

	List<IFieldMetaData> getFulltextFields();

	/**
	 * Getter for all none-system fields in this table (not id, version, updated-by...).
	 * 
	 * @return List of all value fields.
	 */
	List<IFieldMetaData> getAllFields();

	/**
	 * Getter for all relations referencing this table.
	 * 
	 * @return List of all relations.
	 */
	List<IDirectedLinkMetaData> getLinks();

	Object getInitialVersion();

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
	IFieldMetaData mapField(String fieldName, String memberName);

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
	IDirectedLinkMetaData mapLink(String linkName, String memberName);

	/**
	 * Getter for a field identified by its name.
	 * 
	 * @param fieldName
	 *            Name of the field to get.
	 * @return Field identified by the given field name.
	 */
	IFieldMetaData getFieldByName(String fieldName);

	/**
	 * Getter for a field identified by the name of the member mapped to this field.
	 * 
	 * @param memberName
	 *            Name of the identifying member.
	 * @return Field mapped to the member.
	 */
	IFieldMetaData getFieldByMemberName(String memberName);

	/**
	 * Getter for a field index identified by the name of the field. The index maps directly to the field instance in the list of getAllFields().
	 * 
	 * @param memberName
	 *            Name of the identifying member.
	 * @return Field index mapped to the member (correlating to the list of getAllFields())
	 */
	int getFieldIndexByName(String fieldName);

	/**
	 * Getter for a field identified by the name of the property (primitive, relation, id, and version) mapped to this field.
	 * 
	 * @param propertyName
	 *            Name of the identifying property.
	 * @return Field mapped to the property.
	 */
	IFieldMetaData getFieldByPropertyName(String propertyName);

	/**
	 * Getter for a link identified by its name.
	 * 
	 * @param linkName
	 *            Name of the link to get.
	 * @return Link identified by the given link name.
	 */
	IDirectedLinkMetaData getLinkByName(String linkName);

	/**
	 * Getter for a link identified by the link containing field name on this table.
	 * 
	 * @param fieldName
	 *            Field name representing a link.
	 * @return Link identified by the given field name.
	 */
	IDirectedLinkMetaData getLinkByFieldName(String fieldName);

	/**
	 * Getter for a link identified by the name of the member mapped to this field.
	 * 
	 * @param memberName
	 *            Name of the identifying member.
	 * @return Link mapped to the member.
	 */
	IDirectedLinkMetaData getLinkByMemberName(String memberName);

	/**
	 * Getter for the member name mapped to a field in this table and referenced by a link identified by the given link name.
	 * 
	 * @param linkName
	 *            Identifying link name.
	 * @return Name of the member referenced by the given link.
	 */
	String getMemberNameByLinkName(String linkName);
}
