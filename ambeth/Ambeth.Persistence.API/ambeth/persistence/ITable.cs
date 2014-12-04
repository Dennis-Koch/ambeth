using De.Osthus.Ambeth.Collections;
using De.Osthus.Ambeth.Merge.Model;
using System;
using System.Collections;
using System.Collections.Generic;
using System.Text;

namespace De.Osthus.Ambeth.Persistence
{
    public interface ITable
    {
        void StartBatch();

        int[] FinishBatch();

        void ClearBatch();

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
        Object Insert(Object id, out Object newId, ILinkedMap<String, Object> puis);

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
        Object Update(Object id, Object version, ILinkedMap<String, Object> puis);

        /**
         * Deletes a list of persisted objects from storage identified by ids and versions.
         * 
         * @param oris
         *            Object references of the objects to delete.
         */
        void Delete(IList<IObjRef> oris);

        /**
         * Truncates this table.
         */
        void DeleteAll();

        /**
         * Pre-processing for delete operations. Updates or deletes the links referenced by the operation.
         * 
         * @param id
         *            Unique ID identifying the object in storage.
         * @param version
         *            Current object version.
         */
        void PreProcessDelete(Object id, Object version);

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
        void PreProcessUpdate(Object id, Object version, IList<IPrimitiveUpdateItem> puis, IList<IRelationUpdateItem> ruis);

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
        void PostProcessInsertAndUpdate(Object id, Object version, List<IPrimitiveUpdateItem> puis, List<IRelationUpdateItem> ruis);

        /**
         * Acquires a list of new and unique IDs for this table.
         * 
         * @param count
         *            Number of IDs to fetch.
         * @return List of new IDs.
         */
        IList<Object> AcquireIds(int count);

        /**
         * Selects ID and version of all entities in table.
         * 
         * @return Cursor to the IDs and versions.
         */
        IVersionCursor SelectAll();

        /**
         * Selects ID and version of all given entities in table.
         * 
         * @param ids
         *            List of IDs identifying the entities to select.
         * @return Cursor to ID and version of all selected entities.
         */
        IVersionCursor SelectVersion(IList ids);

        /**
         * Selects ID and version of all given entities in table.
         * 
         * @param alternateIdMemberName
         *            Name of the alternate ID member.
         * @param alternateIds
         *            List of IDs identifying the entities to select.
         * @return Cursor to ID and version of all selected entities.
         */
        IVersionCursor SelectVersion(String alternateIdMemberName, IList alternateIds);

        /**
         * Selects ID and version according to the given where clause.
         * 
         * @param whereSql
         *            SQL string containing the WHERE clause without the key word
         * @return Cursor to ID and version of all selected entities.
         */
        IVersionCursor SelectVersionWhere(String whereSql);

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
        IVersionCursor SelectVersionWhere(IList<String> additionalSelectColumnList, String whereWithOrderBySql, IList<Object> parameters);

        /**
         * Selects ID, version, and all value fields of all given entities in table.
         * 
         * @param ids
         *            List of IDs identifying the entities to select.
         * @return Cursor to ID, version, and value fields of all selected entities.
         */
        ICursor SelectValues(IList ids);

        /**
         * Selects ID, version, and all value fields of all given entities in table.
         * 
         * @param alternateIdMemberName
         *            Name of alternate ID member.
         * @param alternateIds
         *            Alternate IDs identifying the entities to select.
         * @return Cursor to ID, version, and value fields of all selected entities.
         */
        ICursor SelectValues(String alternateIdMemberName, IList alternateIds);

        /**
         * Getter for table name.
         * 
         * @return Name of this table.
         */
        String Name { get; }

        /**
         * Getter for the table name which is in quotes to allow to include the value directly in a query string
         * 
         * @return
         */
        String FullqualifiedEscapedName { get; }

        bool ViewBased { get; }

        /**
         * Getter for the type of the entities persisted in this table.
         * 
         * @return Type of the persisted entities.
         */
        Type EntityType { get; }

        /**
         * @return True if this is an archive Table, otherwise false.
         */
        bool Archive { get; }

        /**
	     * @return True if this is an permission group table, otherwise false.
	     */
        bool PermissionGroup { get; }

        /**
         * Getter for the ID field of this table.
         * 
         * @return Representation of the primary key of this table.
         */
        IField GetIdField();

        /**
         * Getter for the version field of this table.
         * 
         * @return Representation of the object version of this table.
         */
        IField GetVersionField();

        /**
         * Get all primitive fields which are treated as unique and represent an alternate id for the entity retrieval
         * 
         * @return List of all unique constrained value fields which should be treated like alternate id fields
         */
        IField[] GetAlternateIdFields();

        /**
         * Number of alternate id fields
         * 
         * @return List of all unique constrained value fields which should be treated like alternate id fields
         */
        int GetAlternateIdCount();

        /**
         * Get all indices in the array of primitive fields which are treated as an alternate id
         * 
         * @return array of indices where each value refers to the index in the primitive field array
         */
        short[] GetAlternateIdFieldIndicesInFields();

        /**
         * Get id or alternate id field dependent on the specified idIndex
         * 
         * @param idIndex
         * @return primary id field, if idIndex == -1, otherwise the alternate id field at this index >= 0
         */
        IField GetIdFieldByAlternateIdIndex(sbyte idIndex);

        IField GetCreatedOnField();

        IField GetCreatedByField();

        IField GetUpdatedOnField();

        IField GetUpdatedByField();

        /**
         * Getter for all value fields in this table (not id, version, updated-by, foreign key...).
         * 
         * @return List of all value fields.
         */
        IList<IField> GetPrimitiveFields();

        IList<IField> GetFulltextFields();

        /**
         * Getter for all none-system fields in this table (not id, version, updated-by...).
         * 
         * @return List of all value fields.
         */
        IList<IField> GetAllFields();

        /**
         * Getter for all relations referencing this table.
         * 
         * @return List of all relations.
         */
        IList<IDirectedLink> GetLinks();

        String GetSequenceName();

        /**
         * Maps a field in this table to a member of the entity persisted in this table.
         * 
         * @param fieldName
         *            Name of the field to map.
         * @param memberName
         *            Name of the member to map.
         * @return Mapped field.
         */
        IField MapField(String fieldName, String memberName);

        /**
         * Maps a field or memberName in this table as ignored.
         * 
         * @param fieldName
         *            Name of the field to map.
         * @param memberName
         *            Name of the member to map.
         */
        void MapIgnore(String fieldName, String memberName);

        bool IsIgnoredField(String fieldName);

        bool IsIgnoredMember(String memberName);

        /**
         * Maps a link referencing this table to a member of the entity persisted in this table.
         * 
         * @param linkName
         *            Name of the link to map.
         * @param memberName
         *            Name of the member to map.
         * @return Mapped link.
         */
        IDirectedLink MapLink(String linkName, String memberName);

        /**
         * Getter for a field identified by its name.
         * 
         * @param fieldName
         *            Name of the field to get.
         * @return Field identified by the given field name.
         */
        IField GetFieldByName(String fieldName);

        /**
         * Getter for a field identified by the name of the member mapped to this field.
         * 
         * @param memberName
         *            Name of the identifying member.
         * @return Field mapped to the member.
         */
        IField GetFieldByMemberName(String memberName);

        /**
         * Getter for a field index identified by the name of the field. The index maps directly to the field instance in the list of getAllFields().
         * 
         * @param memberName
         *            Name of the identifying member.
         * @return Field index mapped to the member (correlating to the list of getAllFields())
         */
        int GetFieldIndexByName(String memberName);

        /**
         * Getter for a field identified by the name of the property (primitive, relation, id, and version) mapped to this field.
         * 
         * @param propertyName
         *            Name of the identifying property.
         * @return Field mapped to the property.
         */
        IField GetFieldByPropertyName(String propertyName);

        /**
         * Getter for a link identified by its name.
         * 
         * @param linkName
         *            Name of the link to get.
         * @return Link identified by the given link name.
         */
        IDirectedLink GetLinkByName(String linkName);

        /**
         * Getter for a link identified by the name of the member mapped to this field.
         * 
         * @param memberName
         *            Name of the identifying member.
         * @return Link mapped to the member.
         */
        IDirectedLink GetLinkByMemberName(String memberName);

        /**
         * Getter for the member name mapped to a field in this table and referenced by a link identified by the given link name.
         * 
         * @param linkName
         *            Identifying link name.
         * @return Name of the member referenced by the given link.
         */
        String GetMemberNameByLinkName(String linkName);
    }
}