using De.Osthus.Ambeth.Log;
using System;
using De.Osthus.Ambeth.Util;
using De.Osthus.Ambeth.Config;

namespace De.Osthus.Ambeth.Ioc.Config
{
    public class PropertyEmbeddedRefConfiguration : AbstractPropertyConfiguration
    {
        protected String propertyName;

        protected IBeanConfiguration embeddedBean;

        public PropertyEmbeddedRefConfiguration(IBeanConfiguration parentBeanConfiguration, String propertyName, IBeanConfiguration embeddedBean,
            IProperties props) : base(parentBeanConfiguration, props)
        {
            ParamChecker.AssertParamNotNull(propertyName, "propertyName");
            ParamChecker.AssertParamNotNull(embeddedBean, "embeddedBean");
            this.propertyName = propertyName;
            this.embeddedBean = embeddedBean;
        }

        public PropertyEmbeddedRefConfiguration(IBeanConfiguration parentBeanConfiguration, IBeanConfiguration embeddedBean, IProperties props)
            : base(parentBeanConfiguration, props)
        {
            ParamChecker.AssertParamNotNull(embeddedBean, "embeddedBean");
            this.embeddedBean = embeddedBean;
        }


        public override String GetPropertyName()
        {
            return propertyName;
        }


        public override String GetBeanName()
        {
            return embeddedBean.GetName();
        }


        public override bool IsOptional()
        {
            return false;
        }


        public override Object GetValue()
        {
            return embeddedBean;
        }
    }
}