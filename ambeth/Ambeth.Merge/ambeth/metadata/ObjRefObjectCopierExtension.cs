using De.Osthus.Ambeth.Copy;
using De.Osthus.Ambeth.Ioc.Annotation;
using De.Osthus.Ambeth.Log;
using De.Osthus.Ambeth.Merge.Model;
using System;

namespace De.Osthus.Ambeth.Metadata
{
    public class ObjRefObjectCopierExtension : IObjectCopierExtension
    {
        [LogInstance]
        public ILogger Log { private get; set; }

        [Autowired]
        public IObjRefFactory ObjRefFactory { protected get; set; }

        public Object DeepClone(Object original, IObjectCopierState objectCopierState)
        {
            IObjRef objRef = (IObjRef)original;
            return ObjRefFactory.Dup(objRef);
        }
    }
}