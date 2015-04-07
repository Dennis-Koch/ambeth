package de.osthus.ambeth.persistence;

import de.osthus.ambeth.metadata.Member;
import de.osthus.ambeth.util.INamed;

public interface IFieldMetaData extends INamed
{
	/**
	 * 
	 * @return true, if this IField instance is an alternate id (single column unique constraint) for the related ITable
	 */
	boolean isAlternateId();

	/**
	 * Returns the index this id field (if it is) on the list of ids of a table
	 * 
	 * @return >= 0 if this is an alternate id field
	 */
	byte getIdIndex();

	/**
	 * Returns the index this field is managed on its owning table
	 * 
	 * @return >= 0 if this field is managed by a table
	 */
	int getIndexOnTable();

	/**
	 * Returns the information whether a mapping to a property of an entity is expected. If a mapping is not possible a warning will be logged if the mapping is
	 * expected (flag is true)
	 * 
	 * @return true if a warning should be logged when a property can not be resolved at evaluation time
	 */
	boolean expectsMapping();

	/**
	 * Getter for the parent table.
	 * 
	 * @return Table containing this field.
	 */
	ITableMetaData getTable();

	/**
	 * Getter for the type of the entity persisted in the parent table.
	 * 
	 * @return Entity type.
	 */
	Class<?> getEntityType();

	/**
	 * Getter for the Java type of the value stored in this field.
	 * 
	 * @return Java type of the stored value.
	 */
	Class<?> getFieldType();

	/**
	 * Getter for additional information (like sub-types of a java type, e.g. on java.sql.Arrays of the value stored in this field.
	 * 
	 * @return Additional information about the Java type of the stored value.
	 */
	Class<?> getFieldSubType();

	/**
	 * Getter for the type info of the member linked to this field.
	 * 
	 * @return Type info of the linked member.
	 */
	Member getMember();
}
