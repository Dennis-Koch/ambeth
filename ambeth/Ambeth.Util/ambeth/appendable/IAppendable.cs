using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;

namespace De.Osthus.Ambeth.Appendable
{
    public interface IAppendable
    {
        IAppendable Append(String value);

        IAppendable Append(char value);
    }
}
