using System;

namespace De.Osthus.Ambeth.Typeinfo
{
    public interface IPropertyInfoIntern : IPropertyInfo
    {
        new Type ElementType { get; set; }
    }
}
