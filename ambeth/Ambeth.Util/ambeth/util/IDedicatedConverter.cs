using System;

namespace De.Osthus.Ambeth.Util
{
    public interface IDedicatedConverter
    {
        /// <summary>
        /// Converts a defined set of types.
        /// </summary>
        /// <param name="expectedType">Type to convert to</param>
        /// <param name="sourceType">Type to convert from</param>
        /// <param name="value">Value of class sourceType</param>
        /// <param name="additionalInformation">Optional information if neede for this conversion</param>
        /// <returns>Value converted to expectedType</returns>
        Object ConvertValueToType(Type expectedType, Type sourceType, Object value, Object additionalInformation);
    }
}