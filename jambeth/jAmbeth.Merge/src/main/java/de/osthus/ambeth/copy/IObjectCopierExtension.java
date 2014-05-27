package de.osthus.ambeth.copy;

/**
 * Implement this interface to encapsulate copy logic which extends the default ObjectCopier behavior
 */
public interface IObjectCopierExtension
{
	/**
	 * Implement this interface to encapsulate copy logic which extends the default ObjectCopier behavior
	 * 
	 * @param original
	 *            The object instance to copy
	 * @param objectCopierState
	 *            Encapsulates the current copy state. It may be called in cascaded custom/default copy behaviors
	 * @return The copied object representing a deep clone of the source object
	 */
	Object deepClone(Object original, IObjectCopierState objectCopierState);
}
