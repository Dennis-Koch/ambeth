package de.osthus.ambeth.cache;

/**
 * Delegate interface for use with ICache.getContent() to execute a method with every cached object.
 */
public interface HandleContentDelegate
{
	/**
	 * Method invoked with every cached object.
	 * 
	 * @param entityType
	 *            Entity type.
	 * @param id
	 *            Entity ID.
	 * @param value
	 *            Entity instance.
	 */
	void invoke(Class<?> entityType, byte idIndex, Object id, Object value);
}
