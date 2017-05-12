using System;

namespace De.Osthus.Ambeth.Bytecode.AbstractObject
{
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
        void Register(Type keyType);

        /**
         * Register a type to be implemented by this extension. The implementation will extend baseType
         * 
         * @param baseType
         *            The (abstract) base type to be extended
         * @param keyType
         *            The type to be implemented
         */
        void RegisterBaseType(Type baseType, Type keyType);

        /**
         * Registers a type to be implemented by this extension. The implementation will implement interfaceTypes
         * 
         * @param interfaceTypes
         *            The interface types to be implemented
         * @param keyType
         *            The type to be implemented
         */
        void RegisterInterfaceTypes(Type[] interfaceTypes, Type keyType);

        /**
         * Unregister a type to be implemented by this extension.
         * 
         * @param keyType
         *            The type to be unregistered
         */
        void Unregister(Type keyType);

        /**
         * Unregister a type to be implemented by this extension.
         * 
         * @param baseType
         *            The (abstract) base type to be extended
         * @param keyType
         *            The type to be unregistered
         */
        void UnregisterBaseType(Type baseType, Type keyType);

        /**
         * Unregister a type to be implemented by this extension.
         * 
         * @param interfaceTypes
         *            The interface types to be implemented
         * @param keyType
         *            The type to be unregistered
         */
        void UnregisterInterfaceTypes(Type[] interfaceTypes, Type keyType);
    }
}