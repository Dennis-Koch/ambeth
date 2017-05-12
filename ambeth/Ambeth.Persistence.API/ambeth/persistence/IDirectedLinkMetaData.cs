using De.Osthus.Ambeth.Annotation;
using De.Osthus.Ambeth.Metadata;
using System;

namespace De.Osthus.Ambeth.Persistence
{
    public interface IDirectedLinkMetaData
    {
        ITableMetaData getFromTable();

        IFieldMetaData getFromField();

        Type getFromEntityType();

        byte getFromIdIndex();

        Member getFromMember();

        ITableMetaData getToTable();

        IFieldMetaData getToField();

        Type getToEntityType();

        byte getToIdIndex();

        Member getToMember();

        String getName();

        bool isNullable();

        bool isReverse();

        bool isPersistingLink();

        /**
         * Link _not_ persisted in this table?
         * 
         * @return Standalone status
         */
        bool isStandaloneLink();

        bool isCascadeDelete();

        Type getEntityType();

        RelationMember getMember();

        ILinkMetaData getLink();

        IDirectedLinkMetaData getReverseLink();

        CascadeLoadMode getCascadeLoadMode();
    }
}