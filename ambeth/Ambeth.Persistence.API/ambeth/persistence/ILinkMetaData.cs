using System;

namespace De.Osthus.Ambeth.Persistence
{
    public interface ILinkMetaData
    {
        ITableMetaData getFromTable();

        ITableMetaData getToTable();

        IFieldMetaData getFromField();

        IFieldMetaData getToField();

        bool isNullable();

        bool hasLinkTable();

        IDirectedLinkMetaData getDirectedLink();

        IDirectedLinkMetaData getReverseDirectedLink();

        String getName();

        String getTableName();

        /**
         * Getter for the table name which is in quotes to allow to include the value directly in a query string
         * 
         * @return
         */
        String getFullqualifiedEscapedTableName();

        String getArchiveTableName();
    }
}