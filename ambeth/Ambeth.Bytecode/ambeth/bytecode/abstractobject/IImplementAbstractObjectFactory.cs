using System;

namespace De.Osthus.Ambeth.Bytecode.AbstractObject
{
    /**
     * IImplementAbstractObjectFactory implements objects based on interfaces. Optionally the implementations can inherit from an (abstract) base type
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
        bool IsRegistered(Type keyType);

        /**
         * Returns the base type registered for this type
         * 
         * @param keyType
         *            The type to be implemented
         * @return BaseType or Object.class if the type is not registered with an base type
         * @throws IllegalArgumentException
         *             when the type is not registered
         */
        Type GetBaseType(Type keyType);

        /**
         * Returns interfaces types to be implemented for this type
         * 
         * @param keyType
         *            The type to be implemented
         * @return InterfaceTypes interface types to be implemented
         * @throws IllegalArgumentException
         *             when the type is not registered
         */
        Type[] GetInterfaceTypes(Type keyType);

        /**
         * Creates the implementation of the type. Optionally the implementation can inherit from an (abstract) base base type
         * 
         * @param keyType
         *            The type to be implemented
         * @return The type implementing keyType
         */
        Type GetImplementingType(Type keyType);
    }
}