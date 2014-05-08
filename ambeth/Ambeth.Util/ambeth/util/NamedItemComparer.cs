using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;

namespace De.Osthus.Ambeth.Util
{
    public class NamedItemComparer : IComparer<INamed>
    {
        public int Compare(INamed leftItem, INamed rightItem)
        {
            return leftItem.Name.CompareTo(rightItem.Name);
        }
    }
}
