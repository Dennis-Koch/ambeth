using System;
using System.Reflection;
using De.Osthus.Ambeth.Log;
using De.Osthus.Ambeth.Util;

namespace De.Osthus.Ambeth.Ioc.Link
{
    [Obsolete]
    public class LinkContainerOld : AbstractLinkContainerOld
    {
        [LogInstance]
        public new ILogger Log { private get; set; }
        
        public MethodInfo AddMethod { protected get; set; }

        public MethodInfo RemoveMethod { protected get; set; }

        public override void AfterPropertiesSet()
        {
            base.AfterPropertiesSet();

            ParamChecker.AssertParamNotNull(AddMethod, "AddMethod");
            ParamChecker.AssertParamNotNull(RemoveMethod, "RemoveMethod");
            ParamChecker.AssertTrue(AddMethod.GetParameters().Length == Arguments.Length, "Parameter count does not match");
        }

        protected override void HandleLink(Object registry, Object listener, Object[] args)
        {
            try
            {
                args[0] = listener;
                this.AddMethod.Invoke(registry, args);
            }
            catch (System.Exception)
            {
                throw;
            }
        }

        protected override void HandleUnlink(Object registry, Object listener, Object[] args)
        {
            args[0] = listener;
            this.RemoveMethod.Invoke(registry, args);
        }
    }
}
