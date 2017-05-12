using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;

namespace De.Osthus.Ambeth.Util
{
    public struct LockState
    {
        public int readLockCount, writeLockCount;
    }
}
