using System;
using De.Osthus.Ambeth.Ioc.Config;
namespace De.Osthus.Ambeth.Ioc.Link
{
    public interface ILinkRuntime
    {
        ILinkRuntime With(params Object[] arguments);

        ILinkRuntime Optional();

        void FinishLink();
    }
}