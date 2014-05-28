package de.osthus.ambeth.copy;

/**
 * Performs a deep copy of the object. The ObjectCopier can clone any object tree in a flexible and extendable way. Use IObjectCopierExtendable to provide own
 * extensions to the default ObjectCopier behavior if necessary. In addition the ObjectCopier recognizes native data copy scenarios as well as cyclic paths in
 * the object tree.
 */
public interface IObjectCopier
{
	/**
	 * Performs a deep copy of the object
	 * 
	 * @param <T>
	 *            The type of object being copied
	 * @param source
	 *            The object instance to copy
	 * @return The copied object representing a deep clone of the source object
	 */
	<T> T clone(T source);
}