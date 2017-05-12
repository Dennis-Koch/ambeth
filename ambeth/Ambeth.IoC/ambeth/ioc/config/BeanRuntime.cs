using De.Osthus.Ambeth.Log;
using System;
using De.Osthus.Ambeth.Ioc.Factory;
using System.Collections.Generic;
using De.Osthus.Ambeth.Util;
using De.Osthus.Ambeth.Proxy;
using De.Osthus.Ambeth.Config;

namespace De.Osthus.Ambeth.Ioc.Config
{
    public class BeanRuntime<V> : IBeanRuntime<V>
    {
        protected ServiceContext serviceContext;
        
        protected readonly BeanConfiguration beanConfiguration;

        protected bool joinLifecycle;

        protected V beanInstance;

        protected Type beanType;

        public BeanRuntime(ServiceContext serviceContext, Type beanType, bool joinLifecycle)
        {
            this.serviceContext = serviceContext;
            this.beanType = beanType;
            this.joinLifecycle = joinLifecycle;
            beanConfiguration = CreateBeanConfiguration(beanType);
        }

        public BeanRuntime(ServiceContext serviceContext, V beanInstance, bool joinLifecycle)
        {
            this.serviceContext = serviceContext;
            this.beanInstance = beanInstance;
            this.joinLifecycle = joinLifecycle;
            beanConfiguration = CreateBeanConfiguration(beanInstance.GetType());
        }

        protected virtual BeanConfiguration CreateBeanConfiguration(Type beanType)
	    {
		    IProxyFactory proxyFactory = serviceContext.GetService<IProxyFactory>(false);
            IProperties props = serviceContext.GetService<IProperties>(true);
            return new BeanConfiguration(beanType, null, proxyFactory, props);
	    }

        public V Finish()
        {
            BeanContextFactory beanContextFactory = serviceContext.GetBeanContextFactory();
            IBeanContextInitializer beanContextInitializer = beanContextFactory.GetBeanContextInitializer();
            IList<IBeanConfiguration> beanConfHierarchy = beanContextInitializer.FillParentHierarchyIfValid(serviceContext, beanContextFactory, beanConfiguration);

            V bean = beanInstance;
            if (bean == null)
            {
                Type beanType = this.beanType;
                if (beanType == null)
                {
                    beanType = beanContextInitializer.ResolveTypeInHierarchy(beanConfHierarchy);
                }
                bean = (V)Activator.CreateInstance(beanType);
            }

            bean = (V)beanContextInitializer.InitializeBean(serviceContext, beanContextFactory, beanConfiguration, bean, beanConfHierarchy, joinLifecycle);
            return bean;
        }

        public IBeanRuntime<V> IgnoreProperties(String propertyName)
        {
            beanConfiguration.IgnoreProperties(propertyName);
            return this;
        }

        public IBeanRuntime<V> IgnoreProperties(params String[] propertyNames)
        {
            beanConfiguration.IgnoreProperties(propertyNames);
            return this;
        }

        public IBeanRuntime<V> Parent(String parentBeanTemplateName)
        {
            beanConfiguration.Parent(parentBeanTemplateName);
            return this;
        }

        public IBeanRuntime<V> PropertyRef(String propertyName, String beanName)
        {
            beanConfiguration.PropertyRef(propertyName, beanName);
            return this;
        }

        public IBeanRuntime<V> PropertyRef(String propertyName, IBeanConfiguration bean)
        {
            beanConfiguration.PropertyRef(propertyName, bean);
            return this;
        }

        public IBeanRuntime<V> PropertyRefs(String beanName)
        {
            beanConfiguration.PropertyRefs(beanName);
            return this;
        }

        public IBeanRuntime<V> PropertyRefs(params String[] beanNames)
        {
            beanConfiguration.PropertyRefs(beanNames);
            return this;
        }

        public IBeanRuntime<V> PropertyRef(IBeanConfiguration bean)
        {
            beanConfiguration.PropertyRef(bean);
            return this;
        }

        public IBeanRuntime<V> PropertyValue(String propertyName, Object value)
        {
            beanConfiguration.PropertyValue(propertyName, value);
            return this;
        }
    }
}