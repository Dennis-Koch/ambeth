using System;

namespace De.Osthus.Ambeth.Propertychange
{
    public interface IPropertyChangeExtension
    {
        void PropertyChanged(Object obj, String propertyName, Object oldValue, Object currentValue);
    }
}
