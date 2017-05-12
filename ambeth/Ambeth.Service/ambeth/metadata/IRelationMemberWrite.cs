using De.Osthus.Ambeth.Annotation;
using System;

namespace De.Osthus.Ambeth.Metadata
{
    public interface IRelationMemberWrite
    {
        void SetCascadeLoadMode(CascadeLoadMode cascadeLoadMode);
    }
}
