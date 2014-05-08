using System;

namespace De.Osthus.Ambeth.Ioc.Link
{
    public interface ILinkConfiguration
    {
        ILinkConfiguration With(params Object[] arguments);

        ILinkConfiguration Optional();
    }
}