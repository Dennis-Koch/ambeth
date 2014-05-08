using System;

namespace De.Osthus.Ambeth.Xml.Pending
{
    public interface ICommandTypeExtendable
    {
        void RegisterOverridingCommandType(Type overridingCommandType, Type commandType);

        void UnregisterOverridingCommandType(Type overridingCommandType, Type commandType);
    }
}
