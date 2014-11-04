using De.Osthus.Ambeth.Model;
using System;

namespace De.Osthus.Ambeth.Mixin
{
    public class EmbeddedTypeMixin
    {
        public Object GetRoot(IEmbeddedType embeddedObject)
        {
            Object parent = embeddedObject.Parent;
            while (parent is IEmbeddedType)
            {
                parent = ((IEmbeddedType)parent).Parent;
            }
            return parent;
        }
    }
}
