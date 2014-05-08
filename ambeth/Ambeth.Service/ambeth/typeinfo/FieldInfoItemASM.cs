using System;
using System.Reflection;

namespace De.Osthus.Ambeth.Typeinfo
{
    public class FieldInfoItemASM : FieldInfoItem
    {
        protected MemberGetDelegate getDelegate;

        protected MemberSetDelegate setDelegate;
        
        public FieldInfoItemASM(FieldInfo field) : this(field, true)
        {
            // Intended blank
        }

        public FieldInfoItemASM(FieldInfo field, bool allowNullEquivalentValue)
            : this(field, allowNullEquivalentValue, field.Name)
        {
            // Intended blank
        }

        public FieldInfoItemASM(FieldInfo field, String propertyName)
            : this(field, true, propertyName)
        {
            // Intended blank
        }

        public FieldInfoItemASM(FieldInfo field, bool allowNullEquivalentValue, String propertyName)
            : base(field, allowNullEquivalentValue, propertyName)
        {
            getDelegate = TypeUtility.GetMemberGetDelegate(field.DeclaringType, field.Name);
            setDelegate = TypeUtility.GetMemberSetDelegate(field.DeclaringType, field.Name);
        }

        public override void SetValue(Object obj, Object value)
        {
            if (value == null && allowNullEquivalentValue)
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
