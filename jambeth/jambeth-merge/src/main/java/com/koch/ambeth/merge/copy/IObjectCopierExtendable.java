package com.koch.ambeth.merge.copy;

/**
 * Allows to extend the IObjectCopier with custom copy logic if needed.
 * 
 * Note that inheritance and polymorphism functionality will be supported out-of-the-box. Therefore extensions registering themselves to type 'Collection' will
 * be used when copying a List because List implements Collection. On the other hand the registered extension will not be used when registered to type 'IList'
 * and the object to copy is a HashSet.
 * 
 * Note also that - like all extendable interfaces - the explicit call to these register/unregister methods can be done manually, but should in most scenarios
 * be dedicated to the IOC container where the Extension Point / Extension relation will be connected to the corresponding lifecycle of both components.
 */
public interface IObjectCopierExtendable
{
	/**
	 * Registers the given extension to copy objects which are assignable to the given type
	 * 
	 * @param objectCopierExtension
	 *            The extension which implements custom copy logic
	 * @param type
	 *            The type for which the custom copy logic should be applied
	 */
	void registerObjectCopierExtension(IObjectCopierExtension objectCopierExtension, Class<?> type);

	/**
	 * Unregisters the given extension which copied objects which are assignable to the given type
	 * 
	 * @param objectCopierExtension
	 *            The extension which implements custom copy logic
	 * @param type
	 *            The type for which the custom copy logic should not be applied any more
	 */
	void unregisterObjectCopierExtension(IObjectCopierExtension objectCopierExtension, Class<?> type);
}
