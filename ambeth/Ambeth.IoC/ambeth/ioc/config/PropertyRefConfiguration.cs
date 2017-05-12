using De.Osthus.Ambeth.Log;
using System;
using De.Osthus.Ambeth.Util;
using De.Osthus.Ambeth.Config;

namespace De.Osthus.Ambeth.Ioc.Config
{

    public class PropertyRefConfiguration : AbstractPropertyConfiguration
    {
        protected String propertyName;

        protected String beanName;

        protected bool optional;

        public PropertyRefConfiguration(IBeanConfiguration parentBeanConfiguration, String propertyName, String beanName, IProperties props)
            : this(parentBeanConfiguration, propertyName, beanName, false, props)
        {
            // Intended blank
        }

        public PropertyRefConfiguration(IBeanConfiguration parentBeanConfiguration, String propertyName, String beanName, bool optional, IProperties props)
            : base(parentBeanConfiguration, props)
        {
            ParamChecker.AssertParamNotNull(propertyName, "propertyName");
            ParamChecker.AssertParamNotNull(beanName, "beanName");
            this.propertyName = propertyName;
            this.beanName = beanName;
            this.optional = optional;
        }

        public PropertyRefConfiguration(IBeanConfiguration parentBeanConfiguration, String beanName, IProperties props)
            : this(parentBeanConfiguration, beanName, false, props)
        {
            // Intended blank
        }

        public PropertyRefConfiguration(IBeanConfiguration parentBeanConfiguration, String beanName, bool optional, IProperties props)
            : base(parentBeanConfiguration, props)
        {
            ParamChecker.AssertParamNotNull(beanName, "beanName");
            this.beanName = beanName;
            this.optional = optional;
        }


        public override String GetPropertyName()
        {
            return propertyName;
        }


        public override String GetBeanName()
        {
            return beanName;
        }


        public override bool IsOptional()
        {
            return optional;
        }


        public override Object GetValue()
        {
            return null;
        }
    }
}
