using System;

namespace De.Osthus.Ambeth.Bytecode
{
    public class NoOpValueResolveDelegate : IValueResolveDelegate
    {
	    private readonly Object value;

	    public NoOpValueResolveDelegate(Object value)
	    {
		    this.value = value;
	    }

        public Type ValueType
        {
            get { return value.GetType(); }
        }

        public Object Invoke(String fieldName, Type enhancedType)
        {
            return value;
        }
    }
}
