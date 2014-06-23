using De.Osthus.Ambeth.Merge;
using System;

namespace De.Osthus.Ambeth.Bytecode
{
    /**
     * Entity based on a class with non default constructor. If an entity does not have a default constructor EntityManager expects a constructor having
     * {@link IEntityFactory} as parameter. Other constructors are not used by EntityManager.
     */
    public class EntityC : AbstractEntity
    {
        public virtual long? Id { get; set; }

        protected String name;

        protected String value;

        protected EntityC(IEntityFactory entityFactory)
            : base(entityFactory)
        {
            // intended blank
        }

        public virtual String Name
        {
            get
            {
                return name;
            }
            set
            {
                name = value;
            }
        }

        public virtual String Value
        {
            get
            {
                return value;
            }
            set
            {
                this.value = value;
            }
        }
    }
}