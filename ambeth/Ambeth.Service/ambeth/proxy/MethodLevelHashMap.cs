using De.Osthus.Ambeth.Collections;
using De.Osthus.Ambeth.Util;
using System;
using System.Reflection;

namespace De.Osthus.Ambeth.Proxy
{
    public class MethodLevelHashMap<T> : Tuple2KeyHashMap<String, Type[], T>
    {
	    public MethodLevelHashMap() : base()
	    {
            // intended blank
	    }

	    public MethodLevelHashMap(float loadFactor) : base(loadFactor)
	    {
            // intended blank
	    }

	    public MethodLevelHashMap(int initialCapacity, float loadFactor) : base(initialCapacity, loadFactor)
	    {
            // intended blank
	    }

	    public MethodLevelHashMap(int initialCapacity) : base(initialCapacity)
	    {
            // intended blank
	    }

        protected override bool EqualKeys(String key1, Type[] key2, Tuple2KeyEntry<String,Type[],T> entry)
        {
		    return key1.Equals(entry.GetKey1()) && Arrays.Equals(key2, entry.GetKey2());
	    }

        protected override int ExtractHash(String key1, Type[] key2)
        {
		    return key1.GetHashCode() ^ Arrays.GetHashCode(key2);
	    }

        public void Put(MethodInfo method, T value)
        {
            ParameterInfo[] parameters = method.GetParameters();
            Type[] parameterTypes = new Type[parameters.Length];
            for (int a = parameterTypes.Length; a-- > 0; )
            {
                parameterTypes[a] = parameters[a].ParameterType;
            }
            Put(method.Name, parameterTypes, value);
        }

        public T Get(MethodInfo method)
        {
            ParameterInfo[] parameters = method.GetParameters();
            Type[] parameterTypes = new Type[parameters.Length];
            for (int a = parameterTypes.Length; a-- > 0; )
            {
                parameterTypes[a] = parameters[a].ParameterType;
            }
            return Get(method.Name, parameterTypes);
        }
    }
}