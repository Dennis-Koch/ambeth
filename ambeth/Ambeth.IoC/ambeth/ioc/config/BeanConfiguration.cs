using De.Osthus.Ambeth.Config;
using De.Osthus.Ambeth.Exceptions;
using De.Osthus.Ambeth.Ioc.Exceptions;
using De.Osthus.Ambeth.Ioc.Proxy;
using De.Osthus.Ambeth.Proxy;
using System;

namespace De.Osthus.Ambeth.Ioc.Config
{
    public class BeanConfiguration : AbstractBeanConfiguration
    {
        protected readonly Type beanType;

        protected readonly IProxyFactory proxyFactory;

        protected Object createdInstance;

        protected bool isAbstractConfig;

        public BeanConfiguration(Type beanType, String beanName, IProxyFactory proxyFactory, IProperties props)
            : base(beanName, props)
        {
            this.beanType = beanType;
            this.proxyFactory = proxyFactory;
        }

        public override IBeanConfiguration Template()
        {
            isAbstractConfig = true;
            return this;
        }

        public override bool IsAbstract()
        {
            return isAbstractConfig;
        }

        public override Type GetBeanType()
        {
            return beanType;
        }

        public override Object GetInstance(Type instanceType)
        {
            if (createdInstance == null)
            {
                try
                {
                    if (instanceType.IsInterface)
                    {
                        createdInstance = proxyFactory.CreateProxy(instanceType, EmptyInterceptor.INSTANCE);
                    }
                    else
                    {
                        createdInstance = Activator.CreateInstance(instanceType);
                        if (declarationStackTrace != null && createdInstance is IDeclarationStackTraceAware)
                        {
                            ((IDeclarationStackTraceAware)createdInstance).DeclarationStackTrace = declarationStackTrace;
                        }

                    }
                }
                catch (Exception e)
                {
                    if (declarationStackTrace != null)
                    {
                        throw new BeanContextDeclarationException(declarationStackTrace, e);
                    }
                    else
                    {
                        throw;
                    }
                }
            }
            return createdInstance;
        }
    }
}
