using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using De.Osthus.Ambeth.Typeinfo;
using De.Osthus.Ambeth.Merge.Transfer;
using De.Osthus.Ambeth.Merge.Model;
using De.Osthus.Ambeth.Ioc;

namespace De.Osthus.Ambeth.Merge
{
    public interface ICUDResultHelper
    {
        ICUDResult CreateCUDResult(MergeHandle mergeHandle);
    }
}
