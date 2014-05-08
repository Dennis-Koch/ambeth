using De.Osthus.Ambeth.Copy;
using De.Osthus.Ambeth.Ioc.Annotation;
using De.Osthus.Ambeth.Ioc.Config;
using De.Osthus.Ambeth.Ioc.Factory;
using De.Osthus.Ambeth.Log;
using System.Text;

namespace De.Osthus.Ambeth.Ioc
{
    /// <summary>
    /// Registers an ObjectCopier as well as default extensions to copy objects
    /// Include this module in an IOC container to gain access to <code>IObjectCopier</code> & <code>IObjectCopierExtendable</code> functionality
    /// </summary>
    [FrameworkModule]
    public class ObjectCopierModule : IInitializingModule
    {
        [LogInstance]
        public ILogger Log { private get; set; }

        public void AfterPropertiesSet(IBeanContextFactory beanContextFactory)
        {
            // Default ObjectCopier implementation
            IBeanConfiguration objectCopier = beanContextFactory.RegisterAnonymousBean<ObjectCopier>().Autowireable(typeof(IObjectCopier), typeof(IObjectCopierExtendable));

            // Default ObjectCopier extensions
            IBeanConfiguration stringBuilderOCE = beanContextFactory.RegisterAnonymousBean<StringBuilderOCE>();
            beanContextFactory.Link(stringBuilderOCE).To<IObjectCopierExtendable>().With(typeof(StringBuilder));
        }
    }
}
