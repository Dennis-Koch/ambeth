using De.Osthus.Ambeth.Metadata;
using System;
using System.Collections.Generic;

namespace De.Osthus.Ambeth.Cache.Valueholdercontainer
{
    public class EmbeddedMaterial
    {
        public virtual String Name { get; set; }

        public virtual IList<String> Names { get; set; }

        public virtual MaterialType EmbMatType { get; set; }

        public virtual EmbeddedMaterial2 EmbMat2 { get; set; }
    }
}