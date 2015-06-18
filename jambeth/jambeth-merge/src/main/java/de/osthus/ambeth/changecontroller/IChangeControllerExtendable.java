package de.osthus.ambeth.changecontroller;

/**
 * This interface defines the extendable part of an {@link IChangeController}. It is used to allow components to register themself as extension to the
 * changeController by using Ambeth's link mechanism.
 * 
 * @see IChangeControllerExtension
 */
public interface IChangeControllerExtendable
{
	/**
	 * Register a change controller for all entities that implement the given interface.
	 * 
	 * @param extension
	 *            the extension that should be before entities are persisted.
	 * @param clazz
	 *            an interface, never <code>null</code>
	 */
	void registerChangeControllerExtension(IChangeControllerExtension<?> extension, Class<?> clazz);

	/**
	 * This undoes the registering. The extension is not called anymore for the given interface. If the extension has not been registered for the interface, the
	 * call has no effect.
	 * 
	 * @param extension
	 * @param clazz
	 */
	void unregisterChangeControllerExtension(IChangeControllerExtension<?> extension, Class<?> clazz);
}
