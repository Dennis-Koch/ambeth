using De.Osthus.Ambeth.Collections;
using De.Osthus.Ambeth.Exceptions;
using De.Osthus.Ambeth.Ioc;
using De.Osthus.Ambeth.Threading;
using De.Osthus.Ambeth.Util;
using System;
using System.Reflection;
using System.Reflection.Emit;
using System.Threading;

namespace De.Osthus.Ambeth.Bytecode.Behavior
{
    public class BytecodeBehaviorState : IBytecodeBehaviorState
    {
        public class PropertyKey
	    {
		    private readonly String propertyName;

            private readonly NewType propertyType;

            public PropertyKey(String propertyName, NewType propertyType)
		    {
			    this.propertyName = propertyName;
			    this.propertyType = propertyType;
		    }

		    public override bool Equals(Object obj)
		    {
			    if (Object.ReferenceEquals(obj, this))
			    {
				    return true;
			    }
			    if (!(obj is PropertyKey))
			    {
				    return false;
			    }
			    PropertyKey other = (PropertyKey) obj;
			    return propertyName.Equals(other.propertyName) && (propertyType == null || other.propertyType == null || propertyType.Equals(other.propertyType));
		    }

            public override int GetHashCode()
            {
			    if (propertyType == null)
			    {
                    return propertyName.GetHashCode();
			    }
                return propertyName.GetHashCode() ^ propertyType.GetHashCode();
		    }
	    }

        private static readonly ThreadLocal<IBytecodeBehaviorState> stateTL = new ThreadLocal<IBytecodeBehaviorState>();

        public static IBytecodeBehaviorState State
        {
            get
            {
                return stateTL.Value;
            }
        }

        public static T SetState<T>(Type originalType, Type currentType, NewType newType, IServiceContext beanContext,
            IEnhancementHint context, IResultingBackgroundWorkerDelegate<T> runnable)
        {
            IBytecodeBehaviorState oldState = stateTL.Value;
            stateTL.Value = new BytecodeBehaviorState(currentType, newType, originalType, beanContext, context);
            try
            {
                return runnable.Invoke();
            }
            finally
            {
                if (oldState != null)
                {
                    stateTL.Value = oldState;
                }
                else
                {
                    stateTL.Value = null;
                }
            }
        }

        private readonly IServiceContext beanContext;
        private readonly Type currentType;
        private readonly NewType newType;
        private readonly Type originalType;
        private readonly IEnhancementHint context;

        private readonly HashMap<MethodKeyOfType, MethodInstance> implementedMethods = new HashMap<MethodKeyOfType, MethodInstance>();

        private readonly HashMap<PropertyKey, PropertyInstance> implementedProperties = new HashMap<PropertyKey, PropertyInstance>();

        private readonly HashMap<String, Object> implementedEvents = new HashMap<String, Object>();

        private readonly HashMap<String, FieldInstance> implementedFields = new HashMap<String, FieldInstance>();

        private readonly HashMap<String, IValueResolveDelegate> initializeStaticFields = new HashMap<String, IValueResolveDelegate>();

        public BytecodeBehaviorState(Type currentType, NewType newType, Type originalType, IServiceContext beanContext, IEnhancementHint context)
        {
            this.currentType = currentType;
            this.newType = newType;
            this.originalType = originalType;
            this.beanContext = beanContext;
            this.context = context;
        }

        public Type CurrentType
        {
            get
            {
                return currentType;
            }
        }

        public NewType NewType
        {
            get
            {
                return newType;
            }
        }

        public Type OriginalType
        {
            get
            {
                return originalType;
            }
        }

        public IServiceContext BeanContext
        {
            get
            {
                return beanContext;
            }
        }

        public IEnhancementHint Context
        {
            get
            {
                return context;
            }
        }

        public T GetContext<T>() where T : IEnhancementHint
        {
            return (T) GetContext(typeof(T));
        }

        public Object GetContext(Type contextType)
        {
            return context.Unwrap(contextType);
        }

        public void MethodImplemented(MethodInstance method)
        {
            if (!implementedMethods.PutIfNotExists(new MethodKeyOfType(method.ReturnType, method.Name, method.Parameters), method))
            {
                throw new Exception("Method already implemented: " + method);
            }
        }

        public void FieldImplemented(FieldInstance field)
        {
            if (!implementedFields.PutIfNotExists(field.Name, field))
            {
                throw new Exception("Field already implemented: " + field);
            }
        }

        public void PropertyImplemented(PropertyInstance property)
        {
            if (!implementedProperties.PutIfNotExists(new PropertyKey(property.Name, property.PropertyType), property))
            {
                throw new Exception("Property already implemented: " + property);
            }
        }

        public void EventImplemented(String eventName, EventBuilder eventInfo)
        {
            if (!implementedEvents.PutIfNotExists(eventName, eventInfo))
            {
                throw new Exception("Event already implemented: " + eventInfo);
            }
        }

        public void QueueFieldInitialization(String fieldName, IValueResolveDelegate value)
        {
            if (!initializeStaticFields.PutIfNotExists(fieldName, value))
            {
                throw new Exception("Field already queued for initialization: " + fieldName);
            }
        }

        public PropertyInstance GetProperty(String propertyName, NewType propertyType)
        {
            PropertyInstance pi = implementedProperties.Get(new PropertyKey(propertyName, propertyType));
            if (pi != null)
            {
                return pi;
            }
            BindingFlags flags = BindingFlags.Static | BindingFlags.Instance | BindingFlags.Public | BindingFlags.NonPublic | BindingFlags.FlattenHierarchy;
            PropertyInfo propertyInfo = propertyType != null ? CurrentType.GetProperty(propertyName, flags, null, propertyType.Type, Type.EmptyTypes, new ParameterModifier[0]) : CurrentType.GetProperty(propertyName, flags);
            if (propertyInfo != null)
            {
                pi = new PropertyInstance(propertyInfo);
            }
            if (pi == null)
            {
                MethodInfo m_get = CurrentType.GetMethod("Get" + propertyName, flags);
                if (m_get == null)
                {
                    m_get = CurrentType.GetMethod("get" + propertyName, flags);
                }
                MethodInfo m_set = CurrentType.GetMethod("Set" + propertyName, flags);
                if (m_set == null)
                {
                    m_set = CurrentType.GetMethod("set" + propertyName, flags);
                }
                if (m_get != null || m_set != null)
                {
                    pi = new PropertyInstance(propertyName, m_get != null ? new MethodInstance(m_get) : null, m_set != null ? new MethodInstance(m_set) : null);
                }
            }
            return pi;
        }

        public Object GetAlreadyImplementedEvent(String eventName)
        {
            Object eventInfo = implementedEvents.Get(eventName);
            if (eventInfo != null)
            {
                return eventInfo;
            }
            return CurrentType.GetEvent(eventName, BindingFlags.Static | BindingFlags.Instance | BindingFlags.Public | BindingFlags.NonPublic | BindingFlags.FlattenHierarchy);
        }

        public MethodInstance[] GetAlreadyImplementedMethodsOnNewType()
        {
            return implementedMethods.ToArray();
        }

        public FieldInstance GetAlreadyImplementedField(String fieldName)
        {
            FieldInstance field = implementedFields.Get(fieldName);
            if (field != null)
            {
                return field;
            }
            FieldInfo[] declaredFieldInHierarchy = ReflectUtil.GetDeclaredFieldInHierarchy(CurrentType, fieldName);
            if (declaredFieldInHierarchy != null && declaredFieldInHierarchy.Length > 0)
            {
                field = new FieldInstance(declaredFieldInHierarchy[0]);
            }
            return field;
        }

        public bool HasMethod(MethodInstance method)
        {
            MethodInstance existingMethod = MethodInstance.FindByTemplate(method, true);
            return existingMethod != null && State.NewType.Equals(existingMethod.Owner);
        }

        public bool IsMethodAlreadyImplementedOnNewType(MethodInstance method)
        {
            return implementedMethods.ContainsKey(new MethodKeyOfType(method.ReturnType, method.Name, method.Parameters));
        }

        public void PostProcessCreatedType(Type newType)
	    {
            foreach (Entry<String, IValueResolveDelegate> entry in initializeStaticFields)
            {
                FieldInfo[] fields = ReflectUtil.GetDeclaredFieldInHierarchy(newType, entry.Key);
                if (fields.Length == 0)
                {
                    throw new Exception("Field not found: '" + newType.FullName + "." + entry.Key);
                }
                Object value = entry.Value.Invoke(entry.Key, newType);
                foreach (FieldInfo field in fields)
                {
                    try
                    {
                        field.SetValue(null, value);
                    }
                    catch (Exception e)
                    {
                        throw RuntimeExceptionUtil.Mask(e, "Error occured while setting field: " + field);
                    }
                }
            }
            initializeStaticFields.Clear();
        }
    }
}