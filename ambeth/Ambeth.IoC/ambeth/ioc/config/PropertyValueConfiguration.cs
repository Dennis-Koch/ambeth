using De.Osthus.Ambeth.Config;
using De.Osthus.Ambeth.Log;
using System;

namespace De.Osthus.Ambeth.Ioc.Config
{
    public class PropertyValueConfiguration : AbstractPropertyConfiguration
    {
        protected String propertyName;

        protected Object value;

        public PropertyValueConfiguration(IBeanConfiguration parentBeanConfiguration, String propertyName, Object value, IProperties props)
            : base(parentBeanConfiguration, props)
        {
            this.propertyName = propertyName;
            this.value = value;
        }
        
        public override String GetPropertyName()
        {
            return propertyName;
        }

        public override String GetBeanName()
        {
            return null;
        }

        public override bool IsOptional()
        {
            return false;
        }

        public override Object GetValue()
        {
            return value;
        }
    }
}