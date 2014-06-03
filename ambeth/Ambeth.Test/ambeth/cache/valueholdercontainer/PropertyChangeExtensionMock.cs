using De.Osthus.Ambeth.Collections;
using De.Osthus.Ambeth.Propertychange;
using System;

namespace De.Osthus.Ambeth.Cache.Valueholdercontainer
{
    public class PropertyChangeExtensionMock : IPropertyChangeExtension
    {
        protected readonly IMap<String, int> propertyNameToHitCountMap;

        public PropertyChangeExtensionMock(IMap<String, int> propertyNameToHitCountMap)
        {
            this.propertyNameToHitCountMap = propertyNameToHitCountMap;
        }

        public void PropertyChanged(Object obj, String propertyName, Object oldValue, Object currentValue)
        {
            int hitCount = propertyNameToHitCountMap.Get(propertyName);
            hitCount++;
            propertyNameToHitCountMap.Put(propertyName, hitCount);
        }
    }
}