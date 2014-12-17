using De.Osthus.Ambeth.Util;
using System.Threading;

namespace De.Osthus.Ambeth.Ioc.Threadlocal
{
    public interface IForkState
    {
        void Use(Runnable runnable);
    }
}