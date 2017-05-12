using System;

namespace De.Osthus.Ambeth.Xml.Pending
{
    public interface ICommandTypeRegistry
    {
        Type GetOverridingCommandType(Type commandType);
    }
}
