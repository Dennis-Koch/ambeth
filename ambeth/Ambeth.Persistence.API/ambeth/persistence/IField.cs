using De.Osthus.Ambeth.Metadata;
using De.Osthus.Ambeth.Util;
using System;
using System.Collections;

namespace De.Osthus.Ambeth.Persistence
{
    public interface IField : ILinqFinder, INamed
    {
        /**
         * 
         * @return true, if this IField instance is an alternate id (single column unique constraint) for the related ITable
         */
        bool AlternateId { get; }

        /**
         * Returns the index this id field (if it is) on the list of ids of a table
         * 
         * @return >= 0 if this is an alternate id field
         */
        sbyte IdIndex { get; }

        /**
         * Getter for the parent table.
         * 
         * @return Table containing this field.
         */
        ITable Table { get; }

        /**
         * Getter for the type of the entity persisted in the parent table.
         * 
         * @return Entity type.
         */
        Type EntityType { get; }

        /**
         * Getter for the Java type of the value stored in this field.
         * 
         * @return Java type of the stored value.
         */
        Type FieldType { get; }

        /**
         * Getter for additional information (like sub-types of a java type, e.g. on java.sql.Arrays of the value stored in this field.
         * 
         * @return Additional information about the Java type of the stored value.
         */
        Type FieldSubType { get; }

        /**
         * Getter for the field name.
         * 
         * @return Name of this field.
         */
        new String Name { get; }

        /**
         * Getter for the type info of the member linked to this field.
         * 
         * @return Type info of the linked member.
         */
        Member Member { get; }

        /**
         * Selects ID and version of all entities with a given value in this field.
         * 
         * @param value
         *            Identifying value.
         * @return Cursor to ID and version of all selected entities.
         */
        IVersionCursor FindAll(Object value);

        /**
         * Selects ID and version of all entities with one of the given values in this field.
         * 
         * @param values
         *            Identifying values.
         * @return Cursor to ID and version of all selected entities.
         */
        IVersionCursor FindMany(IList values);

        /**
         * Selects ID and version of the first entity with a given value in this field.
         * 
         * @param value
         *            Identifying value.
         * @return Cursor to ID and version of selected entity.
         */
        IVersionItem FindSingle(Object value);
    }
}