using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;

namespace De.Osthus.Ambeth.Util
{
    /// <summary>
    /// Interface for the Ambeth conversion feature. It is used throughout the framework to convert values to different types in one unified way. Event the most
    /// basic Ambeth context has a bean autowired to this interface. The conversion feature is extendable via the {@wiki wikipedia_en Extensibility_pattern
    /// Extensibility pattern} by implementing the {@link IDedicatedConverter} interface and linking the bean to {@link IDedicatedConverterExtendable}.
    /// </summary>
    public abstract class IConversionHelper
    {
        public abstract bool DateTimeUTC { set; }

        /// <summary>
        /// Primary method to convert values.
        /// </summary>
        /// <typeparam name="T">Conversion target type.</typeparam>
        /// <param name="value">Value to be converted.</param>
        /// <returns>Representation of the given value as the target type.</returns>
        public abstract T ConvertValueToType<T>(Object value);

        /// <summary>
        /// Secondary method to convert values to specific types. Only used if the conversion needs additional informations, e.g. lost generic types, date format, string encoding.
        /// </summary>
        /// <typeparam name="T">Conversion target type.</typeparam>
        /// <param name="value">Value to be converted.</param>
        /// <param name="additionalInformation">Additional information needed for this conversion.</param>
        /// <returns>Representation of the given value as the target type.</returns>
        public abstract T ConvertValueToType<T>(Object value, Object additionalInformation);

        /// <summary>
        /// Primary method to convert values.
        /// </summary>
        /// <param name="expectedType">Conversion target type.</param>
        /// <param name="value">Value to be converted.</param>
        /// <returns>Representation of the given value as the target type.</returns>
        public abstract Object ConvertValueToType(Type expectedType, Object value);

        /// <summary>
        /// Secondary method to convert values to specific types. Only used if the conversion needs additional informations, e.g. lost generic types, date format, string encoding.
        /// </summary>
        /// <param name="expectedType">Conversion target type.</param>
        /// <param name="value">Value to be converted.</param>
        /// <param name="additionalInformation">Additional information needed for this conversion.</param>
        /// <returns>Representation of the given value as the target type.</returns>
        public abstract Object ConvertValueToType(Type expectedType, Object value, Object additionalInformation);
    }
}
