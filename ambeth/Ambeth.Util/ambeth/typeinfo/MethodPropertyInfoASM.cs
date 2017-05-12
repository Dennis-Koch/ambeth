using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using De.Osthus.Ambeth.Util;
using System.Reflection;
using De.Osthus.Ambeth.Exceptions;

namespace De.Osthus.Ambeth.Typeinfo
{
    public class MethodPropertyInfoASM : MethodPropertyInfo
    {
        protected MemberGetDelegate getDelegate;

        protected MemberSetDelegate setDelegate;

        public MethodPropertyInfoASM(Type entityType, String propertyName, MethodInfo getter, MethodInfo setter)
            : base(entityType, propertyName, getter, setter)
        {
            if (Getter != null)
            {
                getDelegate = TypeUtility.GetMemberGetDelegate(getter.DeclaringType, getter.Name);
            }
            if (Getter != null)
            {
                setDelegate = TypeUtility.GetMemberSetDelegate(setter.DeclaringType, setter.Name);
            }
            IsReadable = getDelegate != null;
            IsWritable = setDelegate != null;
        }

	    public override void RefreshAccessors(Type realType)
	    {
		    base.RefreshAccessors(realType);
            if (Getter != null)
            {
                getDelegate = TypeUtility.GetMemberGetDelegate(Getter.DeclaringType, Getter.Name);
            }
            if (Getter != null)
            {
                setDelegate = TypeUtility.GetMemberSetDelegate(Getter.DeclaringType, Setter.Name);
            }
            IsReadable = getDelegate != null;
		    IsWritable = setDelegate != null;
	    }

        public override void SetValue(Object obj, Object value)
        {
            try
            {
                setDelegate(obj, value);
            }
            catch (Exception e)
            {
                if (setDelegate == null)
                {
                    throw new NotSupportedException();
                }
                throw RuntimeExceptionUtil.Mask(e, "Error occured while calling '" + Setter + "' on object '" + obj + "' of type '" + obj.GetType().ToString()
                        + "' with argument '" + value + "'");
            }
        }
                
        public override Object GetValue(Object obj)
        {
            try
            {
                return getDelegate(obj);
            }
            catch (Exception e)
            {
                if (getDelegate == null)
                {
                    throw new NotSupportedException();
                }
                throw RuntimeExceptionUtil.Mask(e, "Error occured while calling '" + Getter + "' on object '" + obj + "' of type '" + obj.GetType().ToString()
                        + "'");
            }
        }
    }
}