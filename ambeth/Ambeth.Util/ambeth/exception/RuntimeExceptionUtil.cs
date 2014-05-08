using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;

namespace De.Osthus.Ambeth.Exceptions
{
    public class RuntimeExceptionUtil
    {
        public static readonly String EMPTY_STACK_TRACE = "";

        public static Exception Mask(Exception e, String message)
	    {
            //if (e is InvocationTargetException)
            //{
            //    return mask(((InvocationTargetException) e).getTargetException(), message);
            //}
		    if (e is MaskingRuntimeException && e.Message == null)
		    {
			    return Mask(e.InnerException, message);
		    }
            MaskingRuntimeException re = new MaskingRuntimeException(message, EMPTY_STACK_TRACE, e);
		    return re;
	    }

        public static Exception CreateEnumNotSupportedException(Enum enumInstance)
	    {
		    return new EnumNotSupportedException(enumInstance);
	    }

        private RuntimeExceptionUtil()
        {
            // Intended blank
        }
    }
}
