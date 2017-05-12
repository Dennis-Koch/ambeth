using System;
using System.Collections;
using System.Globalization;
using System.Reflection;

namespace De.Osthus.Ambeth.Util
{
    public class ConversionHelper : IConversionHelper
    {
        protected readonly PropertyInfo nullableValueProperty = typeof(Nullable<>).GetProperty("Value");

        protected readonly PropertyInfo nullableHasValueProperty = typeof(Nullable<>).GetProperty("HasValue");

        protected bool dateTimeUTC;

        public override bool DateTimeUTC
        {
            set
            {
                dateTimeUTC = value;
            }
        }

        public override T ConvertValueToType<T>(Object value)
        {
            return (T)ConvertValueToType(typeof(T), value, null);
        }

        public override T ConvertValueToType<T>(Object value, Object additionalInformation)
        {
            return (T)ConvertValueToType(typeof(T), value, additionalInformation);
        }

        public override Object ConvertValueToType(Type expectedType, Object value)
        {
            return ConvertValueToType(expectedType, value, null);
        }

        public override Object ConvertValueToType(Type expectedType, Object value, Object additionalInformation)
        {
            if (expectedType == null)
            {
                return value;
            }
            if (value == null)
            {
                if (expectedType.IsValueType)
                {
                    return Activator.CreateInstance(expectedType);
                }
                return null;
            }
            Type type = value.GetType();
            if (expectedType.IsAssignableFrom(type))
            {
                return value;
            }
            if (type.IsGenericType && typeof(Nullable<>).Equals(type.GetGenericTypeDefinition()))
            {
                //Convert from Nullable<T>
                bool hasValue = (bool)nullableHasValueProperty.GetValue(value, null);
                if (hasValue)
                {
                    Object notNullablevalue = nullableValueProperty.GetValue(value, null);
                    return ConvertValueToType(expectedType, notNullablevalue);
                }
                else
                {
                    return ConvertValueToType(expectedType, null);
                }
            }
            else if (expectedType.IsGenericType && typeof(Nullable<>).Equals(expectedType.GetGenericTypeDefinition()))
            {
                //Convert to Nullable<T>
                Type[] expectedNotNullableType = expectedType.GetGenericArguments();
                Object notNullableExpectedValue = ConvertValueToType(expectedNotNullableType[0], value);
                Type nullableType = typeof(Nullable<>).MakeGenericType(expectedNotNullableType);
                return Activator.CreateInstance(nullableType, notNullableExpectedValue);
            }
            if (expectedType.IsArray)
            {
                Type elementType = expectedType.GetElementType();
                if (elementType.IsAssignableFrom(value.GetType()))
                {
                    // The type of the array matches already the given value
                    // So we create an array with a size of 1 and put the value in it
                    Array arraySingleValue = Array.CreateInstance(elementType, 1);
                    arraySingleValue.SetValue(value, 0);
                    return arraySingleValue;
                }
                if (value is String)
                {
                    if (typeof(byte).Equals(elementType))
                    {
                        // Special case blob-array
                        return Convert.FromBase64String((String)value);
                    }
                    else if (typeof(Type).Equals(elementType))
                    {
                        // Special case type-array
                        String[] items = ((String)value).Split(';');
                        value = items;
                    }
                }
                IList list = (IList)value;
                Array objectValue = Array.CreateInstance(elementType, list.Count);
                for (int a = list.Count; a-- > 0; )
                {
                    Object convertedItem = ConvertValueToType(elementType, list[a]);
                    objectValue.SetValue(convertedItem, a);
                }
                return objectValue;
            }
            if (typeof(Int64).Equals(expectedType) || typeof(Int64?).Equals(expectedType))
            {
                if (typeof(UInt64).Equals(type))
                {
                    return (Int64)((UInt64)value);
                }
                else if (typeof(Int32).Equals(type))
                {
                    return (Int64)((Int32)value);
                }
                else if (typeof(UInt32).Equals(type))
                {
                    return (Int64)((UInt32)value);
                }
                else if (typeof(Int16).Equals(type))
                {
                    return (Int64)((Int16)value);
                }
                else if (typeof(UInt16).Equals(type))
                {
                    return (Int64)((UInt16)value);
                }
                else if (typeof(DateTime).Equals(type))
                {
                    return DateTimeUtil.ConvertDateTimeToJavaMillis((DateTime)value);
                }
                else if (typeof(String).Equals(type))
                {
                    return Int64.Parse((String)value, CultureInfo.InvariantCulture);
                }
            }
            else if (typeof(Double).Equals(expectedType) || typeof(Double?).Equals(expectedType))
            {
                if (typeof(Int32).Equals(type))
                {
                    return (Double)((Int32)value);
                }
                else if (typeof(UInt32).Equals(type))
                {
                    return (Double)((UInt32)value);
                }
                else if (typeof(String).Equals(type))
                {
                    return Double.Parse((String)value, CultureInfo.InvariantCulture);
                }
            }
            else if (typeof(Int32).Equals(expectedType) || typeof(Int32?).Equals(expectedType))
            {
                if (typeof(Int64).Equals(type))
                {
                    return (Int32)((Int64)value);
                }
                else if (typeof(UInt64).Equals(type))
                {
                    return (Int32)((UInt64)value);
                }
                if (typeof(UInt32).Equals(type))
                {
                    return (Int32)((UInt32)value);
                }
                else if (typeof(Int16).Equals(type))
                {
                    return (Int32)((Int16)value);
                }
                else if (typeof(UInt16).Equals(type))
                {
                    return (Int32)((UInt16)value);
                }
                else if (typeof(String).Equals(type))
                {
                    return Int32.Parse((String)value, CultureInfo.InvariantCulture);
                }
            }
            else if (typeof(UInt32).Equals(expectedType) || typeof(UInt32?).Equals(expectedType))
            {
                if (typeof(Int64).Equals(type))
                {
                    return (UInt32)((Int64)value);
                }
                else if (typeof(UInt64).Equals(type))
                {
                    return (UInt32)((UInt64)value);
                }
                if (typeof(Int32).Equals(type))
                {
                    return (UInt32)((Int32)value);
                }
                else if (typeof(Int16).Equals(type))
                {
                    return (UInt32)((Int16)value);
                }
                else if (typeof(UInt16).Equals(type))
                {
                    return (UInt32)((UInt16)value);
                }
                else if (typeof(String).Equals(type))
                {
                    return UInt32.Parse((String)value, CultureInfo.InvariantCulture);
                }
            }
            else if (typeof(Single).Equals(expectedType) || typeof(Single?).Equals(expectedType))
            {
                if (typeof(Int32).Equals(type))
                {
                    return (Single)((Int32)value);
                }
                else if (typeof(UInt32).Equals(type))
                {
                    return (Single)((UInt32)value);
                }
                else if (typeof(String).Equals(type))
                {
                    return Single.Parse((String)value, CultureInfo.InvariantCulture);
                }
            }
            else if (typeof(Int16).Equals(expectedType) || typeof(Int16?).Equals(expectedType))
            {
                if (typeof(Int64).Equals(type))
                {
                    return (Int16)((Int64)value);
                }
                else if (typeof(UInt64).Equals(type))
                {
                    return (Int16)((UInt64)value);
                }
                else if (typeof(Int32).Equals(type))
                {
                    return (Int16)((Int32)value);
                }
                else if (typeof(UInt32).Equals(type))
                {
                    return (Int16)((UInt32)value);
                }
                else if (typeof(UInt16).Equals(type))
                {
                    return (Int16)((UInt16)value);
                }
                else if (typeof(String).Equals(type))
                {
                    return Int16.Parse((String)value, CultureInfo.InvariantCulture);
                }
            }
            else if (typeof(UInt16).Equals(expectedType) || typeof(UInt16?).Equals(expectedType))
            {
                if (typeof(Int64).Equals(type))
                {
                    return (UInt16)((Int64)value);
                }
                else if (typeof(UInt64).Equals(type))
                {
                    return (UInt16)((UInt64)value);
                }
                else if (typeof(Int32).Equals(type))
                {
                    return (UInt16)((Int32)value);
                }
                else if (typeof(UInt32).Equals(type))
                {
                    return (UInt16)((UInt32)value);
                }
                else if (typeof(UInt16).Equals(type))
                {
                    return (UInt16)((UInt16)value);
                }
                else if (typeof(String).Equals(type))
                {
                    return UInt16.Parse((String)value, CultureInfo.InvariantCulture);
                }
            }
            else if (typeof(Char).Equals(expectedType) || typeof(Char?).Equals(expectedType))
            {
#if !SILVERLIGHT
                if (typeof(String).Equals(type))
                {
                    return Char.Parse((String)value);
                }
#endif
            }
            else if (typeof(Byte).Equals(expectedType) || typeof(Byte?).Equals(expectedType))
            {
                if (typeof(String).Equals(type))
                {
                    return Byte.Parse((String)value, CultureInfo.InvariantCulture);
                }
            }
            else if (typeof(SByte).Equals(expectedType) || typeof(SByte?).Equals(expectedType))
            {
                if (typeof(String).Equals(type))
                {
                    return SByte.Parse((String)value, CultureInfo.InvariantCulture);
                }
            }
            else if (typeof(Boolean).Equals(expectedType) || typeof(Boolean?).Equals(expectedType))
            {
                if (typeof(String).Equals(type))
                {
                    return Boolean.Parse((String)value);
                }
            }
            else if (typeof(DateTime).Equals(expectedType) || typeof(DateTime?).Equals(expectedType))
            {
                if (typeof(Int64).Equals(type))
                {
                    DateTime dateTime = DateTimeUtil.ConvertJavaMillisToDateTime((Int64)value);
                    if (!dateTimeUTC)
                    {
                        dateTime = dateTime.ToLocalTime();
                    }
                    return dateTime;
                }
                else if (typeof(UInt64).Equals(type))
                {
                    DateTime dateTime = DateTimeUtil.ConvertJavaMillisToDateTime((long)((UInt64)value));
                    if (!dateTimeUTC)
                    {
                        dateTime = dateTime.ToLocalTime();
                    }
                    return dateTime;
                }
                else if (typeof(String).Equals(type))
                {
                    String sValue = (String)value;
                    long lValue;
                    if (Int64.TryParse(sValue, out lValue))
                    {
                        DateTime dateTime = DateTimeUtil.ConvertJavaMillisToDateTime(lValue);
                        if (!dateTimeUTC)
                        {
                            dateTime = dateTime.ToLocalTime();
                        }
                        return dateTime;
                    }
                    return DateTime.Parse(sValue);
                }
            }
            else if (typeof(TimeSpan).Equals(expectedType) || typeof(TimeSpan?).Equals(expectedType))
            {
                if (typeof(Int64).Equals(type))
                {
                    return TimeSpan.FromMilliseconds((Int64)value);
                }
                else if (typeof(Double).Equals(type))
                {
                    return TimeSpan.FromMilliseconds((Double)value);
                }
                else if (typeof(Int32).Equals(type))
                {
                    return TimeSpan.FromMilliseconds((Int32)value);
                }
                else if (typeof(UInt32).Equals(type))
                {
                    return TimeSpan.FromMilliseconds((UInt32)value);
                }
                else if (typeof(String).Equals(type))
                {
                    return TimeSpan.FromMilliseconds(Double.Parse((String)value));
                }
            }
            else if (expectedType.IsEnum)
            {
#if SILVERLIGHT
                if (typeof(String).Equals(type))
                {
                    return Enum.Parse(expectedType, (String)value, false);
                }
#else
                if (typeof(String).Equals(type))
                {
                    return Enum.Parse(expectedType, (String)value);
                }
#endif
                else if (typeof(Enum).IsAssignableFrom(type))
                {
                    return ConvertValueToType(expectedType, Enum.GetName(type, value));
                }
            }
            else if (typeof(Type).Equals(expectedType))
            {
                if (typeof(String).Equals(type))
                {
                    Type loadedType = AssemblyHelper.GetTypeFromAssemblies((String)value);
                    if (loadedType == null)
                    {
                        throw new TypeLoadException("Type " + value + " not found in the assemblies of the current application domain");
                    }
                    return loadedType;
                }
            }
            else if (typeof(String).Equals(expectedType))
            {
                if (typeof(Type).Equals(type))
                {
                    return ((Type)value).FullName;
                }
                else if (typeof(IFormattable).IsAssignableFrom(type))
                {
                    return ((IFormattable)value).ToString(null, CultureInfo.InvariantCulture);
                }
                else
                {
                    return value.ToString();
                }
            }
			else if (typeof(char[]).Equals(expectedType))
			{
				if (typeof(String).Equals(type))
				{
					return value.ToString().ToCharArray();
				}
			}
            throw new ArgumentException("Cannot convert from '" + value.GetType() + "' to '" + expectedType + "'");
        }
    }
}
