using System;
using System.Collections.Generic;

namespace De.Osthus.Ambeth.Persistence
{
    /**
     * Interface of the jAmbeth database wrapper. Used for database related methods and accessing the database meta data via jAmbeth.
     * 
     * @author dennis.koch
     */
    public interface IDatabaseMetaData
    {
        IEnumerable<Type> GetHandledEntities();


        /**
         * Getter for the database pool this instance is managed by.
         * 
         * @return Database pool.
         */
        IDatabasePool GetPool();


        /**
         * 
         * @return Name of this database.
         */
        String Name { get; }


        /**
         * 
         * @return Names of schemas in use.
         */
        String[] SchemaNames { get; }

        /**
         * @return Maximum characters this database instance supports as a symbol name
         */
        int GetMaxNameLength();

        /**
         * Getter for alle tables in this database.
         * 
         * @return List of all tables in this database.
         */
        IList<ITableMetaData> GetTables();

        /**
         * Getter for all defined relations in this database.
         * 
         * @return List of all relations defined for the content of this database.
         */
        IList<ILinkMetaData> GetLinks();

        /**
         * Getter for a table identified by the persisted entity type.
         * 
         * @param entityType
         *            Identifying type.
         * @return Table used for persisting the given entity type.
         */
        ITableMetaData GetTableByType(Type entityType);

        /**
         * Getter for an archive table identified by the persisted entity type.
         * 
         * @param entityType
         *            Identifying type.
         * @return Table used for archiving the given entity type.
         */
        ITableMetaData GetArchiveTableByType(Type entityType);

        /**
         * Getter for a permission group table table identified by either an entity table or a link table.
         * 
         * @param tableName
         *            name of either an entity table or a link table
         * @return The permission group table
         */
        IPermissionGroup GetPermissionGroupOfTable(String tableName);

        /**
         * Maps a table identified by name to a given entity type.
         * 
         * @param tableName
         *            Name of the table to map.
         * @param entityType
         *            Type to map to a table.
         * @return Mapped table.
         */
        ITableMetaData MapTable(String tableName, Type entityType);

        /**
         * Maps a table identified by name as archive table to a given entity type.
         * 
         * @param tableName
         *            Name of the table to map.
         * @param entityType
         *            Type to map to a table.
         * @return Mapped table.
         */
        ITableMetaData MapArchiveTable(String tableName, Type entityType);

        /**
         * Maps a table identified by name as permission group table to a given entity type.
         * 
         * @param tableName
         *            Name of the table to map.
         * @param entityType
         *            Type to map to a table.
         */
        void MapPermissionGroupTable(ITableMetaData permissionGroupTable, ITableMetaData targetTable);

        /**
         * Getter for a table identified by name.
         * 
         * @param tableName
         *            Identifying name.
         * @return Table of given name.
         */
        ITableMetaData GetTableByName(String tableName);

        /**
         * Getter for a link identified by name.
         * 
         * @param linkName
         *            Identifying name.
         * @return Link of given name.
         */
        ILinkMetaData GetLinkByName(String linkName);

        /**
         * 
         * @param linkSource
         *            Name of the link-defining thing (link table or foreign key constraint)
         * @return Link defined by the given name.
         */
        ILinkMetaData GetLinkByDefiningName(String definingName);

        void AddLinkByTables(ILinkMetaData link);

        /**
         * 
         * @param table1
         * @param table2
         * @return Links connection the two tables.
         */
        ILinkMetaData[] GetLinksByTables(ITableMetaData table1, ITableMetaData table2);
    }
}