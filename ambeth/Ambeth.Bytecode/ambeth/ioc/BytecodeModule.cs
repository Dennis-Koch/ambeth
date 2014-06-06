using System;
using De.Osthus.Ambeth.Bytecode;
using De.Osthus.Ambeth.Bytecode.Behavior;
using De.Osthus.Ambeth.Bytecode.Core;
using De.Osthus.Ambeth.Cache;
using De.Osthus.Ambeth.Event;
using De.Osthus.Ambeth.Ioc.Annotation;
using De.Osthus.Ambeth.Ioc.Config;
using De.Osthus.Ambeth.Ioc.Factory;

namespace De.Osthus.Ambeth.Ioc
{
    [FrameworkModule]
    public class BytecodeModule : IInitializingModule
    {
        public void AfterPropertiesSet(IBeanContextFactory beanContextFactory)
        {
            beanContextFactory.RegisterBean<BytecodeEnhancer>("bytecodeEnhancer").Autowireable(typeof(IBytecodeEnhancer), typeof(IBytecodeBehaviorExtendable));

            IBeanConfiguration bytecodeClassLoaderBC = beanContextFactory.RegisterAnonymousBean<BytecodeClassLoader>().Autowireable(typeof(IBytecodeClassLoader),
                    typeof(IBytecodePrinter));
            beanContextFactory.Link(bytecodeClassLoaderBC).To<IEventListenerExtendable>().With(typeof(ClearAllCachesEvent)).Optional();
        }

        public static IBeanConfiguration AddDefaultBytecodeBehavior<T>(IBeanContextFactory beanContextFactory) where T : IBytecodeBehavior
        {
            return AddDefaultBytecodeBehavior(beanContextFactory, typeof(T));
        }

        public static IBeanConfiguration AddDefaultBytecodeBehavior(IBeanContextFactory beanContextFactory, Type behaviorType)
        {
            IBeanConfiguration behaviorBC = beanContextFactory.RegisterAnonymousBean(behaviorType);
            beanContextFactory.Link(behaviorBC).To<IBytecodeBehaviorExtendable>();
            return behaviorBC;
        }
    }
}
