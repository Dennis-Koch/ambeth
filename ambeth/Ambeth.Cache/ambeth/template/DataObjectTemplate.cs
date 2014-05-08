using De.Osthus.Ambeth.Cache;
using De.Osthus.Ambeth.Ioc.Annotation;
using De.Osthus.Ambeth.Merge;
using De.Osthus.Ambeth.Model;
using System;

namespace De.Osthus.Ambeth.Template
{
    public class DataObjectTemplate
    {
        [Autowired]
        public ICacheModification CacheModification { protected get; set; }

        [Autowired]
        public IRevertChangesHelper RevertChangesHelper { protected get; set; }
        
        public void ToBeUpdatedChanged(IDataObject obj, bool previousValue, bool currentValue)
        {
            if (previousValue && !currentValue && !CacheModification.ActiveOrFlushing)
            {
                RevertChangesHelper.RevertChanges(obj);
            }
        }
    }
}
