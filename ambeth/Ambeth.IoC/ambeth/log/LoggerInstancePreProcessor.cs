using De.Osthus.Ambeth.Annotation;
using De.Osthus.Ambeth.Collections;
using De.Osthus.Ambeth.Config;
using De.Osthus.Ambeth.Ioc;
using De.Osthus.Ambeth.Ioc.Config;
using De.Osthus.Ambeth.Ioc.Exceptions;
using De.Osthus.Ambeth.Ioc.Factory;
using De.Osthus.Ambeth.Typeinfo;
using System;
using System.Collections.Generic;
using System.Reflection;

namespace De.Osthus.Ambeth.Log
{
    public class LogInstanceAnnotationCache : AnnotationCache<LogInstanceAttribute>
    {
        protected override bool AnnotationEquals(LogInstanceAttribute left, LogInstanceAttribute right)
        {
            return Object.Equals(left.Value, right.Value);
        }
    }

    public class LoggerInstancePreProcessor : SmartCopyMap<Type, ILogger>, IBeanPreProcessor, ILoggerCache
    {
        protected readonly AnnotationCache<LogInstanceAttribute> logInstanceCache = new LogInstanceAnnotationCache();
        
        protected readonly Object lockHandle = new Object();

        public void PreProcessProperties(IBeanContextFactory beanContextFactory, IServiceContext beanContext, IProperties props, String beanName, Object service, Type beanType, IList<IPropertyConfiguration> propertyConfigs, IPropertyInfo[] properties)
        {
            ScanForLogField(props, service, beanType, service.GetType());
        }

        protected void ScanForLogField(IProperties props, Object service, Type beanType, Type type)
        {
            if (type == null || typeof(Object).Equals(type))
            {
                return;
            }
            ScanForLogField(props, service, beanType, type.BaseType);
            FieldInfo[] fields = type.GetFields(BindingFlags.DeclaredOnly | BindingFlags.Instance | BindingFlags.Public | BindingFlags.NonPublic);
            for (int a = fields.Length; a-- > 0; )
            {
                FieldInfo field = fields[a];
                if (!field.FieldType.Equals(typeof(ILogger)))
                {
                    continue;
                }
                ILogger logger = GetLoggerIfNecessary(props, beanType, field);
                if (logger == null)
                {
                    continue;
                }
#if SILVERLIGHT
            if (!field.IsPublic)
            {
                throw new BeanContextInitException("Field '" + field + "' not public but annotated with '" + typeof(LogInstanceAttribute).FullName + "'. This is not valid in Silverlight. One possibility is to set the field public or annotate a public setter property.");
            }
#endif
                field.SetValue(service, logger);
            }
            PropertyInfo[] properties = type.GetProperties(BindingFlags.DeclaredOnly | BindingFlags.Instance | BindingFlags.Public | BindingFlags.NonPublic);
            for (int a = properties.Length; a-- > 0; )
            {
                PropertyInfo property = properties[a];
                if (!property.PropertyType.Equals(typeof(ILogger)))
                {
                    continue;
                }
                ILogger logger = GetLoggerIfNecessary(props, beanType, property);
                if (logger == null)
                {
                    continue;
                }
                MethodInfo setMethod = property.GetSetMethod();
#if SILVERLIGHT
            if (setMethod != null && !setMethod.IsPublic)
            {
                throw new BeanContextInitException("Property '" + property.Name + "' not public but annotated with '" + typeof(LogInstanceAttribute).FullName
                    + "'. This is not valid in Silverlight. One possibility is to set the field public or annotate a public setter property");
            }
#endif
                if (!property.CanWrite || setMethod == null)
                {
                    throw new BeanContextInitException("Property '" + property.Name + "' is not writable but annotated with '" + typeof(LogInstanceAttribute).FullName
                        + "'. This is not valid. One possibility is specifying an accessible setter property");
                }
                property.SetValue(service, logger, (Object[])null);
            }
        }

        protected ILogger GetLoggerIfNecessary(IProperties props, Type beanType, MemberInfo memberInfo)
        {
            LogInstanceAttribute logInstance = logInstanceCache.GetAnnotation(memberInfo);
            if (logInstance == null)
            {
                return null;
            }
            Type loggerBeanType = memberInfo.DeclaringType;
            if (logInstance.Value != null && !typeof(void).Equals(logInstance.Value))
            {
                loggerBeanType = logInstance.Value;
            }
            return GetCachedLogger(props, loggerBeanType);
        }

        public ILogger GetCachedLogger(IServiceContext serviceContext, Type loggerBeanType)
        {
            ILogger logger = Get(loggerBeanType);
            if (logger != null)
            {
                return logger;
            }
            return GetCachedLogger(serviceContext.GetService<IProperties>(), loggerBeanType);
        }

        public ILogger GetCachedLogger(IProperties properties, Type loggerBeanType)
        {
            ILogger logger = Get(loggerBeanType);
            if (logger != null)
            {
                return logger;
            }
            Object writeLock = GetWriteLock();
            lock (writeLock)
            {
                logger = Get(loggerBeanType);
                if (logger != null)
                {
                    // Concurrent thread might have been faster
                    return logger;
                }
                logger = LoggerFactory.GetLogger(loggerBeanType, properties);
                Put(loggerBeanType, logger);
                return logger;
            }
        }
    }
}