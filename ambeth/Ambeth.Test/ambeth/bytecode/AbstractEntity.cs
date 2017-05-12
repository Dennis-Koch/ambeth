using De.Osthus.Ambeth.Merge;

namespace De.Osthus.Ambeth.Bytecode
{
    /**
     * Base class for entities that have access to the {@link IEntityFactory}
     */
    public abstract class AbstractEntity
    {
        protected IEntityFactory entityFactory;

        protected AbstractEntity(IEntityFactory entityFactory)
        {
            this.entityFactory = entityFactory;
        }
    }
}
