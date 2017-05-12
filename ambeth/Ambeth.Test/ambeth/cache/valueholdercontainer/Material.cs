using De.Osthus.Ambeth.Annotation;
using De.Osthus.Ambeth.Debug;
using System;
using System.Collections.Generic;
using System.Diagnostics;

namespace De.Osthus.Ambeth.Cache.Valueholdercontainer
{
    [EntityEqualsAspect]
    public class Material : AbstractMaterial
    {
        public virtual long? Id { get; set; }

        public virtual Object Id2 { get; set; }

        public virtual int Version { get; set; }

        public virtual String Name { get; set; }

        public virtual IList<String> Names { get; set; }

        [ParentChild]
        public virtual MaterialType ChildMatType { get; set; }

        [ParentChild]
        public virtual IList<MaterialType> ChildMatTypes { get; set; }

        public virtual EmbeddedMaterial EmbMat { get; set; }

        public virtual EmbeddedMaterial EmbMat3 { get; set; }
    }
}