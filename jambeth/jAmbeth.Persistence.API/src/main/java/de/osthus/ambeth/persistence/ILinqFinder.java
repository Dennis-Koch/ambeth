package de.osthus.ambeth.persistence;

public interface ILinqFinder
{
	/**
	 * Selects ID and version of all entities with a given value in this field.
	 * 
	 * @param value
	 *            Identifying value.
	 * @return Cursor to ID and version of all selected entities.
	 */
	IVersionCursor all(Object value);

	/**
	 * Selects ID and version of the only entity with a given value in this field. If there are no or more than one entities an exception is thrown.
	 * 
	 * @param value
	 *            Identifying value.
	 * @return Cursor to ID and version of all selected entities.
	 */
	IVersionItem single(Object value) throws IllegalResultException;

	/**
	 * Selects ID and version of the first entity with a given value in this field. If there is no entity an exception is thrown.
	 * 
	 * @param value
	 *            Identifying value.
	 * @return Cursor to ID and version of all selected entities.
	 */
	IVersionItem first(Object value) throws IllegalResultException;

	/**
	 * Selects ID and version of the first entity with a given value in this field. If there is no entity null is returned.
	 * 
	 * @param value
	 *            Identifying value.
	 * @return Cursor to ID and version of all selected entities.
	 */
	IVersionItem firstOrDefault(Object value);
}
