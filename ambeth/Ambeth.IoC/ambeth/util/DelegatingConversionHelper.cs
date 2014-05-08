using System;
using De.Osthus.Ambeth.Config;
using De.Osthus.Ambeth.Ioc;
using De.Osthus.Ambeth.Log;

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
            Object targetValue = value;
            while (true)
            {
                Type targetClass = targetValue.GetType();
                IDedicatedConverter dedicatedConverter = GetExtension(targetClass, expectedType);
                if (dedicatedConverter == null)
                {
                    break;
                }
                Object newTargetValue = dedicatedConverter.ConvertValueToType(expectedType, targetClass, targetValue, additionalInformation);
                if (newTargetValue == null)
                {
                    throw new Exception("It is not allowed that an instance of " + typeof(IDedicatedConverter).FullName + " returns null like "
                            + dedicatedConverter + " did");
                }
                if (expectedType.IsAssignableFrom(newTargetValue.GetType()))
                {
                    return newTargetValue;
                }
                if (newTargetValue.GetType().Equals(targetValue.GetType()))
                {
                    throw new Exception("It is not allowed that an instance of " + typeof(IDedicatedConverter).FullName
                            + " returns a value of the same type (" + newTargetValue.GetType().FullName + ") after conversion like " + dedicatedConverter
                            + " did");
                }
                targetValue = newTargetValue;
            }
            return DefaultConversionHelper.ConvertValueToType(expectedType, targetValue, additionalInformation);
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