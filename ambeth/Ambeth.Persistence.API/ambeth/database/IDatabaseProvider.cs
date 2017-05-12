using De.Osthus.Ambeth.Persistence;
using De.Osthus.Ambeth.Util;
using System.Threading;

namespace De.Osthus.Ambeth.Database
{
    public interface IDatabaseProvider
    {
        ThreadLocal<IDatabase> GetDatabaseLocal();

        IDatabase TryGetInstance();

        IDatabase AcquireInstance();

        IDatabase AcquireInstance(bool readOnly);
    }
}