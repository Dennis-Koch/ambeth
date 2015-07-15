using De.Osthus.Ambeth.Collections;
using System;
using System.Runtime.CompilerServices;

namespace De.Osthus.Ambeth.Util
{
	public class AlreadyHandledSet : Tuple2KeyHashMap<Object, PrefetchPath[], bool?>
	{
		protected override bool EqualKeys(Object obj, PrefetchPath[] prefetchPaths, Tuple2KeyEntry<Object, PrefetchPath[], bool?> entry)
		{
			return obj == entry.GetKey1() && prefetchPaths == entry.GetKey2();
		}

		protected override int ExtractHash(Object obj, PrefetchPath[] prefetchPaths)
		{
			if (prefetchPaths == null)
			{
				return RuntimeHelpers.GetHashCode(obj);
			}
			return RuntimeHelpers.GetHashCode(obj) ^ prefetchPaths.GetHashCode();
		}
	}
}