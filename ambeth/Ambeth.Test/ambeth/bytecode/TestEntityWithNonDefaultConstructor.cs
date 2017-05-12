using System;

namespace De.Osthus.Ambeth.Bytecode
{
    public abstract class TestEntityWithNonDefaultConstructor
    {
        protected int id;

        protected short version;

        protected String name;

        protected TestEntityWithNonDefaultConstructor()
        {
            name = GetHashCode() + "";
        }

        protected TestEntityWithNonDefaultConstructor(String name)
        {
            this.name = name;
        }

        public int Id
        {
            get
            {
                return id;
            }
            set
            {
                id = value;
            }
        }

        public short Version
        {
            get
            {
            return version;
            }
            set
            {
                version = value;
            }
        }

        public String Name
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
    }
}