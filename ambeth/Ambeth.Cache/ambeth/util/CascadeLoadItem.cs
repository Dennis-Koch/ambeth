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
    public class CascadeLoadItem
    {
        public readonly Type realType;

        public readonly DirectValueHolderRef valueHolder;

        public readonly CachePath[] cachePaths;

        public CascadeLoadItem(Type realType, DirectValueHolderRef valueHolder, CachePath[] cachePaths)
        {
            this.realType = realType;
            this.valueHolder = valueHolder;
            this.cachePaths = cachePaths;
        }

        public override int GetHashCode()
        {
            return valueHolder.GetHashCode() ^ cachePaths.GetHashCode();
        }

        public override bool Equals(object obj)
        {
            if (Object.ReferenceEquals(this, obj))
            {
                return true;
            }
            if (!(obj is CascadeLoadItem))
            {
                return false;
            }
            CascadeLoadItem other = (CascadeLoadItem)obj;
            if (valueHolder is DirectValueHolderRef)
		    {
			    // Use equals() of ValueHolderKey
			    return valueHolder.Equals(other.valueHolder) && cachePaths == other.cachePaths;
		    }
            return Object.ReferenceEquals(valueHolder, other.valueHolder)
                && Object.ReferenceEquals(cachePaths, other.cachePaths);
        }
    }
}