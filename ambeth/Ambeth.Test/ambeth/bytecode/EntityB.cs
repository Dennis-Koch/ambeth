using De.Osthus.Ambeth.Merge;
using System;

namespace De.Osthus.Ambeth.Bytecode
{
    /**
     * Entity based on a class with default and non default constructor
     */
    public abstract class EntityB : EntityA
    {
        protected EntityB()
        {
            // Intended blank
        }

        protected EntityB(String name, String value)
        {
            this.name = name;
            this.value = value;
        }
    }
}