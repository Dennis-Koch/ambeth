using De.Osthus.Ambeth.Collections;
using De.Osthus.Ambeth.Util;
using System;
using System.Collections.Generic;

namespace De.Osthus.Ambeth.Typeinfo
{
    public class PropertyInfoEntry
    {
        public class PropertyInfoComparer : IComparer<IPropertyInfo>
        {
            public int Compare(IPropertyInfo x, IPropertyInfo y)
            {
                return x.Name.CompareTo(y.Name);
            }
        }

        public readonly HashMap<String, IPropertyInfo> map;

        public readonly IPropertyInfo[] properties;

        public PropertyInfoEntry(HashMap<String, IPropertyInfo> map)
        {            
            this.map = map;
            List<IPropertyInfo> pis = new List<IPropertyInfo>(map.ToArray());
            pis.Sort(new PropertyInfoComparer());
            this.properties = ListUtil.ToArray(pis);
        }
    }
}