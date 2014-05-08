using System;

namespace De.Osthus.Ambeth.Xml.Pending
{
    public interface ICommandBuilder
    {
        IObjectCommand Build(ICommandTypeRegistry commandTypeRegistry, IObjectFuture objectFuture, Object parent, params Object[] optionals);
    }
}
