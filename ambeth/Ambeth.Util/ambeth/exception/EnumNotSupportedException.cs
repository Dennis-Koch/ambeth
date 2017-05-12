using System;

namespace De.Osthus.Ambeth.Exceptions
{
    public class EnumNotSupportedException : Exception
    {
	    private readonly Enum enumInstance;

        public EnumNotSupportedException(Enum enumInstance)
            : base("Enum not supported: " + enumInstance)
	    {
		    this.enumInstance = enumInstance;
	    }

        public Enum EnumInstance
	    {
            get
            {
                return enumInstance;
            }
	    }
    }
}
