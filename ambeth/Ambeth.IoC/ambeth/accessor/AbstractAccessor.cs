using System;

namespace De.Osthus.Ambeth.Accessor
{
    public abstract class AbstractAccessor
    {
	    protected AbstractAccessor(Type type, String propertyName)
	    {
		    // Intended blank
	    }

        public abstract bool CanRead { get; }

        public abstract bool CanWrite { get; }

	    public abstract Object GetValue(Object obj);

	    public abstract void SetValue(Object obj, Object value);
    }
}
