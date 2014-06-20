using De.Osthus.Ambeth.Merge;
using System;

namespace De.Osthus.Ambeth.Bytecode
{
    /**
     * Entity based on a class with default constructor
     */
    public abstract class EntityA
    {
        public abstract long? Id { get; }

        protected String name;

        protected String value;

        protected EntityA()
        {
            // Intended blank
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