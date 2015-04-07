using De.Osthus.Ambeth.Annotation;
using De.Osthus.Ambeth.Collections;
using De.Osthus.Ambeth.Ioc.Config;
using De.Osthus.Ambeth.Ioc.Factory;
using De.Osthus.Ambeth.Typeinfo;
using De.Osthus.Ambeth.Util;
using System;
using System.Collections.Generic;
using System.Reflection;

namespace De.Osthus.Ambeth.Ioc.Proxy
{
    public class SelfAnnotationCache : AnnotationCache<Self>
    {
        protected override bool AnnotationEquals(Self left, Self right)
        {
            return Object.ReferenceEquals(left, right);
        }
    }

    public class CallingProxyPostProcessor : IInitializingBean
    {
        protected static readonly MemberInfo[] EMPTY_MEMBERS = new MemberInfo[0];

        protected readonly HashMap<Type, MemberInfo[]> typeToMembersMap = new HashMap<Type, MemberInfo[]>(0.5f);

        protected readonly AnnotationCache<Self> logInstanceCache = new SelfAnnotationCache();

        protected readonly Object readLock, writeLock;

        public IPropertyInfoProvider PropertyInfoProvider { protected get; set; }

        public CallingProxyPostProcessor()
        {
            readLock = new Object();
            writeLock = readLock;
        }

        public virtual void AfterPropertiesSet()
        {
            ParamChecker.AssertNotNull(PropertyInfoProvider, "PropertyInfoProvider");
        }

        public void BeanPostProcessed(IBeanContextFactory beanContextFactory, IServiceContext beanContext, IBeanConfiguration beanConfiguration, Type beanType,
                Object proxyBean, Object targetBean)
        {
            MemberInfo[] members = GetMembers(targetBean.GetType());
            Type proxyBeanType = proxyBean.GetType();
            foreach (MemberInfo member in members)
            {
                if (member is FieldInfo)
                {
                    FieldInfo field = (FieldInfo)member;
                    if (field.FieldType.IsAssignableFrom(proxyBeanType))
                    {
                        // Only if the proxy can be cast to the needed type we use it
                        field.SetValue(targetBean, proxyBean);
                    }
                    else
                    {
                        // Otherwise we choose the target bean itself for injection
                        field.SetValue(targetBean, targetBean);
                    }
                    continue;
                }
                MethodInfo method = (MethodInfo)member;
                ParameterInfo[] parameterTypes = method.GetParameters();
                Object[] parms = new Object[parameterTypes.Length];
                for (int a = parameterTypes.Length; a-- > 0; )
                {
                    if (parameterTypes[a].ParameterType.IsAssignableFrom(proxyBeanType))
                    {
                        // Only if the proxy can be cast to the needed type we use it
                        parms[a] = proxyBean;
                    }
                    else
                    {
                        // Otherwise we choose the target bean itself for injection
                        parms[a] = targetBean;
                    }
                }
                method.Invoke(targetBean, parms);
            }
        }

        protected MemberInfo[] GetMembersIntern(Type type)
	    {
            Object writeLock = this.writeLock;
            lock (writeLock)
            {
                return typeToMembersMap.Get(type);
            }
	    }

        protected MemberInfo[] GetMembers(Type type)
        {
            MemberInfo[] members = GetMembersIntern(type);
            if (members != null)
            {
                return members;
            }
            List<MemberInfo> ownMembers = new List<MemberInfo>();
            ScanForCallingProxyField(type, type, ownMembers);

            members = GetMembersIntern(type);
            if (members != null)
            {
                // discard own members
                return members;
            }
            Object writeLock = this.writeLock;
            lock (writeLock)
            {
                members = GetMembersIntern(type);
                if (members != null)
                {
                    // discard own members
                    return members;
                }
                members = ownMembers.ToArray();
                typeToMembersMap.Put(type, members.Length > 0 ? members : EMPTY_MEMBERS);
                return members.Length > 0 ? members : EMPTY_MEMBERS;
            }
        }

        protected void ScanForCallingProxyField(Type beanType, Type type, List<MemberInfo> targetMembers)
        {
            if (type == null || typeof(Object).Equals(type))
            {
                return;
            }
            ScanForCallingProxyField(beanType, type.BaseType, targetMembers);
            FieldInfo[] fields = type.GetFields(BindingFlags.DeclaredOnly | BindingFlags.Instance | BindingFlags.Public | BindingFlags.NonPublic);
            for (int a = fields.Length; a-- > 0; )
            {
                FieldInfo field = fields[a];
                if (!field.FieldType.IsAssignableFrom(beanType))
                {
                    continue;
                }
                Self callingProxy = logInstanceCache.GetAnnotation(field);
                if (callingProxy == null)
                {
                    continue;
                }
                targetMembers.Add(field);
            }
            MethodInfo[] methods = type.GetMethods(BindingFlags.DeclaredOnly | BindingFlags.Instance | BindingFlags.Public | BindingFlags.NonPublic);
            for (int a = methods.Length; a-- > 0; )
            {
                MethodInfo method = methods[a];
                Self callingProxy = logInstanceCache.GetAnnotation(method);
                if (callingProxy == null)
                {
                    continue;
                }
                if (method.GetParameters().Length == 0)
                {
                    // Methods without parameter can not be invoked here
                    continue;
                }
                targetMembers.Add(method);
            }
        }
    }
}