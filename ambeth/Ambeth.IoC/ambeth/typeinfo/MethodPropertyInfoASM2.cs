using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using De.Osthus.Ambeth.Util;
using System.Reflection;
using De.Osthus.Ambeth.Exceptions;
using De.Osthus.Ambeth.Accessor;

namespace De.Osthus.Ambeth.Typeinfo
{
    public class MethodPropertyInfoASM2 : MethodPropertyInfo
    {
        protected AbstractAccessor accessor;

        public MethodPropertyInfoASM2(Type entityType, String propertyName, MethodInfo getter, MethodInfo setter, AbstractAccessor accessor)
            : base(entityType, propertyName, getter, setter)
        {
            SetAccessor(accessor);
        }

        public void SetAccessor(AbstractAccessor accessor)
        {
            this.accessor = accessor;
            IsReadable = accessor.CanRead;
            IsWritable = accessor.CanWrite;
        }

        public override Object GetValue(Object obj)
        {
            return accessor.GetValue(obj);
        }

        public override void SetValue(Object obj, Object value)
        {
            accessor.SetValue(obj, value);
        }
    }
}