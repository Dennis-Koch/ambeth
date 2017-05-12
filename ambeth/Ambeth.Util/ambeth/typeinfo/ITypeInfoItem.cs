using System;
using De.Osthus.Ambeth.Util;

namespace De.Osthus.Ambeth.Typeinfo
{
    public interface ITypeInfoItem : INamed
    {
        Object DefaultValue { get; set; }

        Object NullEquivalentValue { get; set; }

        Type RealType { get; }

        Type ElementType { get; }

        Type DeclaringType { get; }

        bool CanRead { get; }

        bool CanWrite { get; }

        bool TechnicalMember { get; set; }

        void SetValue(Object obj, Object value);

        Object GetValue(Object obj);

        Object GetValue(Object obj, bool allowNullEquivalentValue);

        V GetAnnotation<V>() where V : Attribute;
                
        String XMLName { get; }

        bool IsXMLIgnore { get; }
    }
}
