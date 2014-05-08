package de.osthus.ambeth.ioc.extendable;

import java.util.Collection;

/**
 * Interface for listener or extension container for unique instances.
 * 
 * @param <V>
 *            Type of the content
 */
public interface IExtendableContainer<V>
{
	/**
	 * Registers an instance of an extension.
	 * 
	 * @param extension
	 *            Instance to be registered
	 */
	void register(V extension);

	/**
	 * Unregisters an instance of an extension.
	 * 
	 * @param extension
	 *            Instance to be unregistered
	 */
	void unregister(V extension);

	/**
	 * Returns an typed array of all registered extensions.
	 * 
	 * @return Array of extension instances
	 */
	V[] getExtensions();

	/**
	 * Fills the collection with all registered extensions.
	 * 
	 * @param targetExtensionList
	 *            Collection to be filled
	 */
	void getExtensions(Collection<V> targetExtensionList);
}
