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
        ITableMetaData MetaData { get; }

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
         * Getter for all relations referencing this table.
         * 
         * @return List of all relations.
         */
        IList<IDirectedLink> GetLinks();
        
        /**
         * Getter for a link identified by its name.
         * 
         * @param linkName
         *            Name of the link to get.
         * @return Link identified by the given link name.
         */
        IDirectedLink GetLinkByName(String linkName);

        /**
         * Getter for a link identified by its field name.
         * 
         * @param linkName
         *            Name of the link to get.
         * @return Link identified by the given field name.
         */
        IDirectedLink GetLinkByFieldName(String fieldName);

        /**
         * Getter for a link identified by the name of the member mapped to this field.
         * 
         * @param memberName
         *            Name of the identifying member.
         * @return Link mapped to the member.
         */
        IDirectedLink GetLinkByMemberName(String memberName);
    }
}