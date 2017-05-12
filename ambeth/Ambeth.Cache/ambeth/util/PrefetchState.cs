using System;

namespace De.Osthus.Ambeth.Util
{
    public class PrefetchState : IPrefetchState
    {
        private readonly Object hardRef;

        public PrefetchState(Object hardRef)
        {
            this.hardRef = hardRef;
        }
    }
}
