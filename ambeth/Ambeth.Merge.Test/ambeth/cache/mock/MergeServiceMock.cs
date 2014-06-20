using De.Osthus.Ambeth.Merge;
using De.Osthus.Ambeth.Merge.Model;
using De.Osthus.Ambeth.Model;
using De.Osthus.Ambeth.Service;
using De.Osthus.Ambeth.Util;
using System;
using System.Collections.Generic;

namespace De.Osthus.Ambeth.Cache.Mock
{
    /**
     * Support for unit tests that do not include jAmbeth.Cache
     */
    public class MergeServiceMock : IMergeService
    {
        public IOriCollection Merge(ICUDResult cudResult, IMethodDescription methodDescription)
        {
            return null;
        }

        public IList<IEntityMetaData> GetMetaData(IList<Type> entityTypes)
        {
            return null;
        }

        public IValueObjectConfig GetValueObjectConfig(Type valueType)
        {
            return null;
        }
    }
}