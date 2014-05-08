using System;
using De.Osthus.Ambeth.Merge.Model;

namespace De.Osthus.Ambeth.Xml.Pending
{
    public class ObjRefFuture : IObjectFuture
    {
        public IObjRef Ori { get; private set; }

        public Object Value { get; set; }

        public ObjRefFuture(IObjRef ori)
        {
            Ori = ori;
        }
    }
}
