using System;
using System.Collections.Generic;
using De.Osthus.Ambeth.Ioc;
using De.Osthus.Ambeth.Merge;
using De.Osthus.Ambeth.Merge.Model;
using De.Osthus.Ambeth.Merge.Transfer;
using De.Osthus.Ambeth.Model;
using De.Osthus.Ambeth.Transfer;
using De.Osthus.Ambeth.Util;

namespace De.Osthus.Ambeth.Service
{
    public class MergeServiceDelegate : IMergeService, IInitializingBean
    {
        public virtual IMergeServiceWCF MergeServiceWCF { get; set; }

        public virtual void AfterPropertiesSet()
        {
            ParamChecker.AssertNotNull(MergeServiceWCF, "MergeServiceWCF");
        }

        public virtual IOriCollection Merge(ICUDResult cudResult, IMethodDescription methodDescription)
        {
            return MergeServiceWCF.Merge((CUDResult)cudResult, (MethodDescription)methodDescription);
        }

        public virtual IList<IEntityMetaData> GetMetaData(IList<Type> entityTypeNames)
        {
            String[] paramWCF = new String[entityTypeNames.Count];
            for (int a = entityTypeNames.Count; a-- > 0; )
            {
                paramWCF[a] = entityTypeNames[a].FullName;
            }
            EntityMetaData[] resultWCF = MergeServiceWCF.GetMetaData(paramWCF);
            List<IEntityMetaData> result = new List<IEntityMetaData>(resultWCF.Length);
            for (int a = 0, size = resultWCF.Length; a < size; a++)
            {
                result.Add(resultWCF[a]);
            }
            return result;
        }

        public virtual IValueObjectConfig GetValueObjectConfig(Type valueType)
        {
            return (IValueObjectConfig) MergeServiceWCF.GetValueObjectConfig(valueType.FullName);
        }
    }
}
