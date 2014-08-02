package de.osthus.ambeth.persistence;

import java.util.List;

import de.osthus.ambeth.metadata.Member;
import de.osthus.ambeth.util.INamed;

public interface IField extends ILinqFinder, INamed
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
	 * Getter for the parent table.
	 * 
	 * @return Table containing this field.
	 */
	ITable getTable();

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
	 * Getter for the field name.
	 * 
	 * @return Name of this field.
	 */
	@Override
	String getName();

	/**
	 * Getter for the type info of the member linked to this field.
	 * 
	 * @return Type info of the linked member.
	 */
	Member getMember();

	/**
	 * Selects ID and version of all entities with a given value in this field.
	 * 
	 * @param value
	 *            Identifying value.
	 * @return Cursor to ID and version of all selected entities.
	 */
	IVersionCursor findAll(Object value);

	/**
	 * Selects ID and version of all entities with one of the given values in this field.
	 * 
	 * @param values
	 *            Identifying values.
	 * @return Cursor to ID and version of all selected entities.
	 */
	IVersionCursor findMany(List<Object> values);

	/**
	 * Selects ID and version of the first entity with a given value in this field.
	 * 
	 * @param value
	 *            Identifying value.
	 * @return Cursor to ID and version of selected entity.
	 */
	IVersionItem findSingle(Object value);
}
