using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;

namespace De.Osthus.Ambeth.Util
{
    public interface IConversionHelper
    {
        bool DateTimeUTC { set; }

        /// <summary>
        /// Primary method to convert values.
        /// </summary>
        /// <typeparam name="T">Conversion target type.</typeparam>
        /// <param name="value">Value to be converted.</param>
        /// <returns>Representation of the given value as the target type.</returns>
	    T ConvertValueToType<T>(Object value);

        /// <summary>
        /// Secondary method to convert values to specific types. Only used if the conversion needs additional informations, e.g. lost generic types, date format, string encoding.
        /// </summary>
        /// <typeparam name="T">Conversion target type.</typeparam>
        /// <param name="value">Value to be converted.</param>
        /// <param name="additionalInformation">Additional information needed for this conversion.</param>
        /// <returns>Representation of the given value as the target type.</returns>
        T ConvertValueToType<T>(Object value, Object additionalInformation);

        /// <summary>
        /// Primary method to convert values.
        /// </summary>
        /// <param name="expectedType">Conversion target type.</param>
        /// <param name="value">Value to be converted.</param>
        /// <returns>Representation of the given value as the target type.</returns>
        Object ConvertValueToType(Type expectedType, Object value);

        /// <summary>
        /// Secondary method to convert values to specific types. Only used if the conversion needs additional informations, e.g. lost generic types, date format, string encoding.
        /// </summary>
        /// <param name="expectedType">Conversion target type.</param>
        /// <param name="value">Value to be converted.</param>
        /// <param name="additionalInformation">Additional information needed for this conversion.</param>
        /// <returns>Representation of the given value as the target type.</returns>
        Object ConvertValueToType(Type expectedType, Object value, Object additionalInformation);
    }
}
