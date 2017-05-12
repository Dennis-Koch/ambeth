using De.Osthus.Ambeth.Metadata;
using System;

namespace De.Osthus.Ambeth.Cache
{
    public interface IParentEntityAware
    {
        void SetParentEntity(Object parentEntity, Member member);
    }
}