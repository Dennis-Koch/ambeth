using De.Osthus.Ambeth.Merge;
using System;

namespace De.Osthus.Ambeth.Proxy
{
    public class EntityFactoryToArgumentConstructor : EntityFactoryConstructor
    {
        private readonly EntityFactoryWithArgumentConstructor constructor;

        private readonly IEntityFactory entityFactory;

        public EntityFactoryToArgumentConstructor(EntityFactoryWithArgumentConstructor constructor, IEntityFactory entityFactory)
        {
            this.constructor = constructor;
            this.entityFactory = entityFactory;
        }

        public override object CreateEntity()
        {
            return constructor.CreateEntity(entityFactory);
        }
    }
}
