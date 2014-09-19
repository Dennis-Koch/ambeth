using De.Osthus.Ambeth.Typeinfo;
using System;

namespace De.Osthus.Ambeth.Accessor
{
    public abstract class AbstractAccessor
    {
        protected AbstractAccessor(Type type, IPropertyInfo property)
	    {
		    // Intended blank
	    }

        public abstract bool CanRead { get; }

        public abstract bool CanWrite { get; }

        public abstract Object GetValue(Object obj, bool allowNullEquivalentValue);

	    public abstract Object GetValue(Object obj);

	    public abstract void SetValue(Object obj, Object value);

        public int GetIntValue(Object obj)
        {
            return ((int)GetValue(obj, true));
        }

        public void SetIntValue(Object obj, int value)
        {
            SetValue(obj, value);
        }
    }
}
