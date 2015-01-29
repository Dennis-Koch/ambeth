using System;
using De.Osthus.Ambeth.Config;
using De.Osthus.Ambeth.Ioc;
using De.Osthus.Ambeth.Log;
using De.Osthus.Ambeth.Exceptions;

namespace De.Osthus.Ambeth.Util
{
    public class DelegatingConversionHelper : ClassTupleExtendableContainer<IDedicatedConverter>, IInitializingBean, IDedicatedConverterExtendable, IConversionHelper
    {
        [LogInstance]
        public ILogger Log { private get; set; }

        public IConversionHelper DefaultConversionHelper { protected get; set; }

        [Property(DateTimeUtil.DateTimeAsUTC, DefaultValue = "true")]
        public bool DateTimeUTC { protected get; set; }

        public DelegatingConversionHelper()
            : base("dedicatedConverter", "type", true)
        {
            // Intended blank
        }

        public virtual void AfterPropertiesSet()
        {
            ParamChecker.AssertNotNull(DefaultConversionHelper, "DefaultConversionHelper");

            DefaultConversionHelper.DateTimeUTC = DateTimeUTC;
        }

        public T ConvertValueToType<T>(Object value)
        {
            return (T)ConvertValueToType(typeof(T), value, null);
        }

        public T ConvertValueToType<T>(Object value, Object additionalInformation)
        {
            return (T)ConvertValueToType(typeof(T), value, additionalInformation);
        }

        public Object ConvertValueToType(Type expectedType, Object value)
        {
            return ConvertValueToType(expectedType, value, null);
        }

        public Object ConvertValueToType(Type expectedType, Object value, Object additionalInformation)
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
            if (expectedType.IsAssignableFrom(value.GetType()))
            {
                return value;
            }
            Object sourceValue = value;
            while (true)
            {
                Type sourceClass = sourceValue.GetType();
                IDedicatedConverter dedicatedConverter = GetExtension(sourceClass, expectedType);
                if (dedicatedConverter == null)
                {
                    break;
                }
                Object targetValue;
                try
                {
                    targetValue = dedicatedConverter.ConvertValueToType(expectedType, sourceClass, sourceValue, additionalInformation);
                }
                catch (Exception e)
                {
                    throw RuntimeExceptionUtil.Mask(e, "Error occured while converting value: " + sourceValue);
                }
                if (targetValue == null)
                {
                    if (expectedType.IsValueType)
                    {
                        throw new Exception("It is not allowed that an instance of " + typeof(IDedicatedConverter).FullName + " returns null like "
                                + dedicatedConverter + " did for conversion from '" + sourceClass.FullName + "' to '" + expectedType + "'");
                    }
                    return null;
                }
                if (expectedType.IsAssignableFrom(targetValue.GetType()))
                {
                    return targetValue;
                }
                if (targetValue.GetType().Equals(sourceValue.GetType()))
                {
                    throw new Exception("It is not allowed that an instance of " + typeof(IDedicatedConverter).FullName
                            + " returns a value of the same type (" + targetValue.GetType().FullName + ") after conversion like " + dedicatedConverter
                            + " did");
                }
                sourceValue = targetValue;
            }
            if (expectedType.IsArray && sourceValue != null && sourceValue.GetType().IsArray)
            {
                // try to convert item by item of the array
                Array sourceArray = (Array)sourceValue;
                int size = sourceArray.GetLength(0);
                Type expectedComponentType = expectedType.GetElementType();
                Array targetValue = Array.CreateInstance(expectedComponentType, size);
                for (int a = sourceArray.GetLength(0); a-- > 0; )
                {
                    Object sourceItem = sourceArray.GetValue(a);
                    Object targetItem = ConvertValueToType(expectedComponentType, sourceItem);
                    targetValue.SetValue(targetItem, a);
                }
                return targetValue;
            }
            return DefaultConversionHelper.ConvertValueToType(expectedType, sourceValue, additionalInformation);
        }

        public void RegisterDedicatedConverter(IDedicatedConverter dedicatedConverter, Type sourceType, Type targetType)
        {
            Register(dedicatedConverter, sourceType, targetType);
        }

        public void UnregisterDedicatedConverter(IDedicatedConverter dedicatedConverter, Type sourceType, Type targetType)
        {
            Unregister(dedicatedConverter, sourceType, targetType);
        }
    }
}