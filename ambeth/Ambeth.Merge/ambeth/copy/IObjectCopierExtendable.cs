using System;

namespace De.Osthus.Ambeth.Copy
{
    /// <summary>
    /// Allows to extend the IObjectCopier with custom copy logic if needed.
    /// 
    /// Note that inheritance and polymorphism functionality will be supported out-of-the-box.
    /// Therefore extensions registering themselves to type 'ICollection' will be used when copying a List because List implements ICollection.
    /// On the other hand the registered extension will not be used when registered to type 'IList' and the object to copy is a HashSet.
    ///
    /// Note also that - like all extendable interfaces - the explicit call to these register/unregister methods can be done manually, but should
    /// in most scenarios be dedicated to the IOC container where the Extension Point / Extension relation will be connected to the corresponding lifecycle of both components.
    /// </summary>
    public interface IObjectCopierExtendable
    {
        /// <summary>
        /// Registers the given extension to copy objects which are assignable to the given type
        /// </summary>
        /// <param name="objectCopierExtension">The extension which implements custom copy logic</param>
        /// <param name="type">The type for which the custom copy logic should be applied</param>
        void RegisterObjectCopierExtension(IObjectCopierExtension objectCopierExtension, Type type);

        /// <summary>
        /// Unregisters the given extension which copied objects which are assignable to the given type
        /// </summary>
        /// <param name="objectCopierExtension">The extension which implements custom copy logic</param>
        /// <param name="type">The type for which the custom copy logic should not be applied any more</param>
        void UnregisterObjectCopierExtension(IObjectCopierExtension objectCopierExtension, Type type);
    }
}