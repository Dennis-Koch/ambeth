using De.Osthus.Ambeth.Merge;
using System;

namespace De.Osthus.Ambeth.Proxy
{
    public abstract class EntityFactoryWithArgumentConstructor
    {
        public abstract Object CreateEntity(IEntityFactory entityFactory);
    }
}
