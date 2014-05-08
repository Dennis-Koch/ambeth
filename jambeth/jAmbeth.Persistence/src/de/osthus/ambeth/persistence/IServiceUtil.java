package de.osthus.ambeth.persistence;

import java.util.List;

public interface IServiceUtil
{
	/**
	 * TODO JavaDoc comment.
	 * 
	 * @param targetEntities
	 * @param entityType
	 * @param cursor
	 */
	<T> void loadObjects(List<T> targetEntities, Class<T> entityType, ILinkCursor cursor);

	/**
	 * TODO JavaDoc comment.
	 * 
	 * Loads the instances of the objects selected in the cursor into the target entities list. This is the step from having a search result to having the
	 * actual objects searched for.
	 * 
	 * @param targetEntities
	 *            List to store the target objects in.
	 * @param entityType
	 *            Type of the entities to load.
	 * @param cursor
	 *            Version cursor with the IDs and versions of the objects to load.
	 */
	<T> void loadObjectsIntoCollection(List<T> targetEntities, Class<T> entityType, IVersionCursor cursor);

	/**
	 * TODO JavaDoc comment.
	 * 
	 * @param entityType
	 *            Type of the entity to load.
	 * @param item
	 *            Version info (ID and version) of the object to load.
	 * @return If successful Requested object, otherwise null.
	 */
	<T> T loadObject(Class<T> entityType, IVersionItem item);
}
