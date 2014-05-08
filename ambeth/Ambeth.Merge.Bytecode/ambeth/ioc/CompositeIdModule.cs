using De.Osthus.Ambeth.CompositeId;
using De.Osthus.Ambeth.Ioc.Annotation;
using De.Osthus.Ambeth.Ioc.Factory;

namespace De.Osthus.Ambeth.Ioc
{
    [FrameworkModule]
    public class CompositeIdModule : IInitializingModule
    {
        public virtual void AfterPropertiesSet(IBeanContextFactory beanContextFactory)
        {
            beanContextFactory.RegisterBean<CompositeIdFactory>("compositeIdFactory").Autowireable<ICompositeIdFactory>();

            BytecodeModule.AddDefaultBytecodeBehavior<CompositeIdBehavior>(beanContextFactory);
        }
    }
}
