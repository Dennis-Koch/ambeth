using System;

namespace De.Osthus.Ambeth.Typeinfo
{
    public sealed class NullEquivalentValueUtil
    {
        public static Object GetNullEquivalentValue(Type type)
        {
            return Activator.CreateInstance(type);
        }

        private NullEquivalentValueUtil()
        {
            // intended blank
        }
    }
}
