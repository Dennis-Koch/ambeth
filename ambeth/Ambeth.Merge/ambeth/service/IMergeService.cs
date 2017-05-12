using System;
using System.Collections.Generic;
using System.ServiceModel;
using De.Osthus.Ambeth.Merge.Model;
using De.Osthus.Ambeth.Model;
using De.Osthus.Ambeth.Annotation;
using De.Osthus.Ambeth.Merge;

namespace De.Osthus.Ambeth.Service
{
    [XmlType]
    public interface IMergeService
    {
        IOriCollection Merge(ICUDResult cudResult, IMethodDescription methodDescription);

        IList<IEntityMetaData> GetMetaData(IList<Type> entityTypes);

    	IValueObjectConfig GetValueObjectConfig(Type valueType);
    }
}
