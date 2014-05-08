package de.osthus.ambeth.bytecode.abstractobject;

/**
 * IImplementAbstractObjectFactory implements objects based on interfaces. Optionally the implementations can inherit
 * from an (abstract) base type
 */
public interface IImplementAbstractObjectFactory
{
	/**
	 * Returns true if this type is registered for this factory
	 * 
	 * @param keyType
	 *            The type to be implemented
	 * @return true if this type is registered for this factory
	 */
	boolean isRegistered(Class<?> keyType);

	/**
	 * Returns the base type registered for this type. The base type as a(n abstract) class that is extended when
	 * creating the types implementation.
	 * 
	 * @param keyType
	 *            The type to be implemented
	 * @return BaseType or Object.class if the type is not registered with an base type
	 * @throws IllegalArgumentException
	 *             when the type is not registered
	 */
	Class<?> getBaseType(Class<?> keyType);

	/**
	 * Returns interfaces types to be implemented for this type
	 * 
	 * @param keyType
	 *            The type to be implemented
	 * @return InterfaceTypes interface types to be implemented
	 * @throws IllegalArgumentException
	 *             when the type is not registered
	 */
	Class<?>[] getInterfaceTypes(Class<?> keyType);

	/**
	 * Creates the implementation of the type. Optionally the implementation can inherit from an (abstract) base base
	 * type
	 * 
	 * @param keyType
	 *            The type to be implemented
	 * @return The type implementing keyType
	 */
	<T> Class<? extends T> getImplementingType(Class<T> keyType);
}