using De.Osthus.Ambeth.Util;
using System;
using System.Reflection;

namespace De.Osthus.Ambeth.Accessor
{
    public class DefaultAccessor : AbstractAccessor
    {
        private static readonly Object[] EMPTY_ARGS = new Object[0];

        protected readonly MethodInfo getter, setter;

        protected readonly bool readable, writable;

        public DefaultAccessor(Type type, String propertyName)
            : base(type, propertyName)
        {
            getter = ReflectUtil.GetDeclaredMethod(true, type, "get_" + propertyName);
            if (getter == null)
            {
                getter = ReflectUtil.GetDeclaredMethod(true, type, "Get" + propertyName);
            }
            if (getter == null)
            {
                getter = ReflectUtil.GetDeclaredMethod(true, type, "Is" + propertyName);
            }
            setter = ReflectUtil.GetDeclaredMethod(true, type, "set_" + propertyName, new Type[] { null });
            if (setter == null)
            {
                setter = ReflectUtil.GetDeclaredMethod(true, type, "Set" + propertyName, new Type[] { null });
            }
            readable = getter != null && getter.IsPublic;
            writable = setter != null && setter.IsPublic;
        }

        public override bool CanRead()
        {
            return readable;
        }

        public override bool CanWrite()
        {
            return writable;
        }

        public override Object GetValue(Object obj)
        {
            return getter.Invoke(obj, EMPTY_ARGS);
        }

        public override void SetValue(Object obj, Object value)
        {
            setter.Invoke(obj, new Object[] { value });
        }
    }
}