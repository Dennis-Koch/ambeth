using System;

namespace De.Osthus.Ambeth.Typeinfo
{
    public class PropertyInfoItemASM : PropertyInfoItem
    {
        protected MemberGetDelegate getDelegate;

        protected MemberSetDelegate setDelegate;

        public PropertyInfoItemASM(IPropertyInfo property) : this(property, true)
        {
            // Intended blank
        }

        public PropertyInfoItemASM(IPropertyInfo property, bool allowNullEquivalentValue)
            : base(property, allowNullEquivalentValue)
        {
            if (property.IsReadable)
            {
                getDelegate = TypeUtility.GetMemberGetDelegate(property.DeclaringType, property.Name);
            }
            if (property.IsWritable)
            {
                setDelegate = TypeUtility.GetMemberSetDelegate(property.DeclaringType, property.Name);
            }
        }

        public override void SetValue(Object obj, Object value)
        {
            if (value == null && AllowNullEquivalentValue)
            {
                value = NullEquivalentValue;
            }
            setDelegate(obj, value);
        }

        public override Object GetValue(Object obj, bool allowNullEquivalentValue)
        {
            Object value = getDelegate(obj);
            Object nullEquivalentValue = this.NullEquivalentValue;
            if (nullEquivalentValue != null && nullEquivalentValue.Equals(value))
            {
                if (allowNullEquivalentValue)
                {
                    return nullEquivalentValue;
                }
                return null;
            }
            return value;
        }
    }
}