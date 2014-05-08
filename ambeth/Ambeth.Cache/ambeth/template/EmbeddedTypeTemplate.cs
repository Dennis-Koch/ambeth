using De.Osthus.Ambeth.Model;
using System;

namespace De.Osthus.Ambeth.Template
{
    public class EmbeddedTypeTemplate
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
