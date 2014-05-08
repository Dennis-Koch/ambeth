using System;
using System.Reflection;

namespace De.Osthus.Ambeth.Typeinfo
{
    public class FieldPropertyInfoASM : FieldPropertyInfo
    {
        protected MemberGetDelegate getDelegate;

        protected MemberSetDelegate setDelegate;

        public FieldPropertyInfoASM(Type entityType, String propertyName, FieldInfo field)
            : base(entityType, propertyName, field)
        {
            getDelegate = TypeUtility.GetMemberGetDelegate(field.DeclaringType, field.Name);
            setDelegate = TypeUtility.GetMemberSetDelegate(field.DeclaringType, field.Name);
        }

        public override Object GetValue(Object obj)
        {
            return getDelegate(obj);
        }

        public override void SetValue(Object obj, Object value)
        {
            setDelegate(obj, value);
        }
    }
}
