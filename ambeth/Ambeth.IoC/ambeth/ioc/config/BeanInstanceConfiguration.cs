using System;
using De.Osthus.Ambeth.Log;
using De.Osthus.Ambeth.Util;
using De.Osthus.Ambeth.Config;

namespace De.Osthus.Ambeth.Ioc.Config
{

    public class BeanInstanceConfiguration : AbstractBeanConfiguration
    {
        protected Object bean;

        protected bool withLifecycle;

        public BeanInstanceConfiguration(Object bean, String beanName, bool withLifecycle, IProperties props)
            : base(beanName, props)
        {
            ParamChecker.AssertParamNotNull(bean, "bean");
            this.bean = bean;
            this.withLifecycle = withLifecycle;
            if (withLifecycle && declarationStackTrace != null && bean is IDeclarationStackTraceAware)
		    {
			    ((IDeclarationStackTraceAware) bean).DeclarationStackTrace = declarationStackTrace;
		    }
        }

        public override Type GetBeanType()
        {
            return bean.GetType();
        }

        public override Object GetInstance()
        {
            return bean;
        }

        public override Object GetInstance(Type instanceType)
        {
            return bean;
        }
        
        public override bool IsWithLifecycle()
        {
            return withLifecycle;
        }
    }
}