package com.koch.ambeth.bytecode.abstractobject;

/**
 * IImplementAbstractObjectFactoryExtendable provides configuration for {@link IImplementAbstractObjectFactory} to implement objects based on interfaces.
 * Optionally the implementations can inherit from an (abstract) base type
 */
public interface IImplementAbstractObjectFactoryExtendable
{
	/**
	 * Register a type to be implemented by this extension
	 * 
	 * @param keyType
	 *            The type to be implemented
	 */
	void register(Class<?> keyType);

	/**
	 * Register a type to be implemented by this extension. The implementation will extend baseType
	 * 
	 * @param baseType
	 *            The (abstract) base type to be extended
	 * @param keyType
	 *            The type to be implemented
	 */
	void registerBaseType(Class<?> baseType, Class<?> keyType);

	/**
	 * Registers a type to be implemented by this extension. The implementation will implement interfaceTypes
	 * 
	 * @param interfaceTypes
	 *            The interface types to be implemented
	 * @param keyType
	 *            The type to be implemented
	 */
	void registerInterfaceTypes(Class<?>[] interfaceTypes, Class<?> keyType);

	/**
	 * Unregister a type to be implemented by this extension.
	 * 
	 * @param keyType
	 *            The type to be unregistered
	 */
	void unregister(Class<?> keyType);

	/**
	 * Unregister a type to be implemented by this extension.
	 * 
	 * @param baseType
	 *            The (abstract) base type to be extended
	 * @param keyType
	 *            The type to be unregistered
	 */
	void unregisterBaseType(Class<?> baseType, Class<?> keyType);

	/**
	 * Unregister a type to be implemented by this extension.
	 * 
	 * @param interfaceTypes
	 *            The interface types to be implemented
	 * @param keyType
	 *            The type to be unregistered
	 */
	void unregisterInterfaceTypes(Class<?>[] interfaceTypes, Class<?> keyType);
}
