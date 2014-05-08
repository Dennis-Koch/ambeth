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
    public class AlreadyHandledItem
    {
        public readonly Object obj;

        public readonly IList<CachePath> cachePaths;

        public AlreadyHandledItem(Object obj, IList<CachePath> cachePaths)
        {
            this.obj = obj;
            this.cachePaths = cachePaths;
        }

        public override int GetHashCode()
        {
            int objHash = System.Runtime.CompilerServices.RuntimeHelpers.GetHashCode(obj);

            if (cachePaths == null)
            {
                return objHash;
            }
            return objHash ^ cachePaths.GetHashCode();
        }

        public override bool Equals(object obj)
        {
            if (Object.ReferenceEquals(this, obj))
            {
                return true;
            }
            if (!(obj is AlreadyHandledItem))
            {
                return false;
            }
            AlreadyHandledItem other = (AlreadyHandledItem)obj;
            return Object.ReferenceEquals(this.obj, other.obj)
                && Object.ReferenceEquals(cachePaths, other.cachePaths);
        }
    }
}