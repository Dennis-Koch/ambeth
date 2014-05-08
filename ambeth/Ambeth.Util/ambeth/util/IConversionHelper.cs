using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;

namespace De.Osthus.Ambeth.Util
{
    public interface IConversionHelper
    {
        bool DateTimeUTC { set; }

	    T ConvertValueToType<T>(Object value);

        T ConvertValueToType<T>(Object value, Object additionalInformation);

        Object ConvertValueToType(Type expectedType, Object value);

        Object ConvertValueToType(Type expectedType, Object value, Object additionalInformation);
    }
}
