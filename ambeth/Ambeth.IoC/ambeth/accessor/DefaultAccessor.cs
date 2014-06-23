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

        public DefaultAccessor(Type type, String propertyName, Type propertyType)
            : base(type, propertyName)
        {
            getter = ReflectUtil.GetDeclaredMethod(true, type, propertyType, "get_" + propertyName);
            if (getter == null)
            {
                getter = ReflectUtil.GetDeclaredMethod(true, type, propertyType, "Get" + propertyName);
            }
            if (getter == null)
            {
                getter = ReflectUtil.GetDeclaredMethod(true, type, propertyType, "Is" + propertyName);
            }
            setter = ReflectUtil.GetDeclaredMethod(true, type, propertyType, "set_" + propertyName, propertyType);
            if (setter == null)
            {
                setter = ReflectUtil.GetDeclaredMethod(true, type, null, "Set" + propertyName, propertyType);
            }
            readable = getter != null && getter.IsPublic;
            writable = setter != null && setter.IsPublic;
        }

        public override bool CanRead
        {
            get
            {
                return readable;
            }
        }

        public override bool CanWrite
        {
            get
            {
                return writable;
            }
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