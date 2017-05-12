using System;

namespace De.Osthus.Ambeth.Merge
{
    public interface ITransactionState
    {
        bool IsTransactionActive { get; }

        bool? ExternalTransactionManagerActive { get; set; }
    }
}
