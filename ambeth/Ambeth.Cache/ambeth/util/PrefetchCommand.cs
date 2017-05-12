using System;
using System.Collections;
using System.Collections.Generic;
using System.Reflection;
using De.Osthus.Ambeth.Log;
using De.Osthus.Ambeth.Util;
using De.Osthus.Ambeth.Transfer;
using De.Osthus.Ambeth.Merge.Transfer;
using System.Collections.ObjectModel;
using De.Osthus.Ambeth.Cache;
using System.Text;
using De.Osthus.Ambeth.Merge.Model;
using De.Osthus.Ambeth.Ioc;
using De.Osthus.Ambeth.Merge;
using De.Osthus.Ambeth.Typeinfo;

namespace De.Osthus.Ambeth.Util
{
    public class PrefetchCommand
    {
        public readonly DirectValueHolderRef valueHolder;

        public readonly PrefetchPath[] prefetchPaths;

        public PrefetchCommand(DirectValueHolderRef valueHolder, PrefetchPath[] prefetchPaths)
        {
            this.valueHolder = valueHolder;
            this.prefetchPaths = prefetchPaths;
        }

        public override int GetHashCode()
        {
            return valueHolder.GetHashCode() ^ prefetchPaths.GetHashCode();
        }

        public override bool Equals(object obj)
        {
            if (Object.ReferenceEquals(this, obj))
            {
                return true;
            }
            if (!(obj is PrefetchCommand))
            {
                return false;
            }
            PrefetchCommand other = (PrefetchCommand)obj;
            if (valueHolder is DirectValueHolderRef)
		    {
			    // Use equals() of ValueHolderKey
			    return valueHolder.Equals(other.valueHolder) && prefetchPaths == other.prefetchPaths;
		    }
            return Object.ReferenceEquals(valueHolder, other.valueHolder)
                && Object.ReferenceEquals(prefetchPaths, other.prefetchPaths);
        }
    }
}