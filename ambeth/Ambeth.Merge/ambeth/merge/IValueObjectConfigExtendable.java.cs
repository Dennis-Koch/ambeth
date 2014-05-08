using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;

namespace De.Osthus.Ambeth.Merge
{
    public interface IValueObjectConfigExtendable
    {
        void RegisterValueObjectConfig(IValueObjectConfig config);

        void UnregisterValueObjectConfig(IValueObjectConfig config);
    }
}
