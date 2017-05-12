using De.Osthus.Ambeth.Bytecode.AbstractObject;
using De.Osthus.Ambeth.Bytecode.Behavior;
using De.Osthus.Ambeth.Ioc;
using De.Osthus.Ambeth.Ioc.Config;
using De.Osthus.Ambeth.Ioc.Extendable;
using De.Osthus.Ambeth.Ioc.Factory;
using De.Osthus.Ambeth.Merge;
using De.Osthus.Ambeth.Merge.Config;
using De.Osthus.Ambeth.Merge.Model;
using De.Osthus.Ambeth.Orm;
using De.Osthus.Ambeth.Typeinfo;
using De.Osthus.Ambeth.Util;
using De.Osthus.Ambeth.Util.Xml;
using De.Osthus.Ambeth.Xml;
using System;

namespace De.Osthus.Ambeth.Bytecode
{
    public class PublicConstructorVisitorTestModule : IInitializingModule
    {
        private static readonly String IMPLEMENT_ABSTRACT_OBJECT_FACTORY = "implementAbstractObjectFactory";

        public void AfterPropertiesSet(IBeanContextFactory beanContextFactory)
        {
            // creates objects that implement the interfaces
            beanContextFactory.RegisterBean(IMPLEMENT_ABSTRACT_OBJECT_FACTORY, typeof(ImplementAbstractObjectFactory)).Autowireable(
                    typeof(IImplementAbstractObjectFactory), typeof(IImplementAbstractObjectFactoryExtendable));

            BytecodeModule.AddDefaultBytecodeBehavior(beanContextFactory, typeof(ImplementAbstractObjectBehavior)).PropertyRef("ImplementAbstractObjectFactory",
                    IMPLEMENT_ABSTRACT_OBJECT_FACTORY);
        }
    };
}