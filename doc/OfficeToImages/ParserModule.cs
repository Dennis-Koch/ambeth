using De.Osthus.Ambeth.Ioc;
using De.Osthus.Ambeth.Ioc.Annotation;
using De.Osthus.Ambeth.Ioc.Config;
using De.Osthus.Ambeth.Ioc.Factory;
using System;

namespace OfficeToImages
{
    [BootstrapModule]
    public class ParserModule : IInitializingModule
    {
        public void AfterPropertiesSet(IBeanContextFactory beanContextFactory)
        {
            beanContextFactory.RegisterAnonymousBean<ParserController>().Autowireable<IFileParserExtendable>();

            IBeanConfiguration visioParser = beanContextFactory.RegisterAnonymousBean<VisioParser>();
            beanContextFactory.Link(visioParser).To<IFileParserExtendable>().With("vsd");

            IBeanConfiguration powerpointParser = beanContextFactory.RegisterAnonymousBean<PowerpointParser>();
            beanContextFactory.Link(powerpointParser).To<IFileParserExtendable>().With("ppt");
            beanContextFactory.Link(powerpointParser).To<IFileParserExtendable>().With("pptx");
        }
    }
}
