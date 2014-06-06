using System;
using De.Osthus.Ambeth.Ioc.Annotation;
using De.Osthus.Ambeth.Ioc.Extendable;
using De.Osthus.Ambeth.Ioc.Factory;
using De.Osthus.Ambeth.Log;
using De.Osthus.Ambeth.Util;
using De.Osthus.Ambeth.Xml;
using De.Osthus.Ambeth.Xml.Namehandler;
using De.Osthus.Ambeth.Xml.Pending;
using De.Osthus.Ambeth.Xml.PostProcess;
using De.Osthus.Ambeth.Xml.Typehandler;

namespace De.Osthus.Ambeth.Ioc
{
    [FrameworkModule]
    public class XmlModule : IInitializingModule
    {
        [LogInstance]
        public ILogger Log { private get; set; }

        public virtual IServiceContext BeanContext { get; set; }

        public virtual void AfterPropertiesSet(IBeanContextFactory beanContextFactory)
        {
            ParamChecker.AssertNotNull(BeanContext, "BeanContext");
            //if (Log.InfoEnabled)
            //{
            //    Log.Info("Looking for Ambeth bootstrap modules in classpath...");
            //}
            //ClasspathScanner ownClasspathScanner = BeanContext.RegisterAnonymousBean<ClasspathScanner>().propertyValue("PackageFilterPatterns", ".+")
            //        .finish();
            //IList<Type> autoApplicationModules = ownClasspathScanner.ScanClassesImplementing(typeof(IInitializingBootstrapModule));

            //if (Log.InfoEnabled)
            //{
            //    Log.Info("Found " + autoApplicationModules.Count + " Ambeth modules in classpath to include in bootstrap...");
            //}
            //for (int a = 0, size = autoApplicationModules.Count; a < size; a++)
            //{
            //    Type autoApplicationModule = autoApplicationModules[a];
            //    if (Log.InfoEnabled)
            //    {
            //        Log.Info("Including " + autoApplicationModule.FullName);
            //    }
            //    beanContextFactory.registerAnonymousBean(autoApplicationModule);
            //}

            beanContextFactory.RegisterAnonymousBean<BootstrapScannerModule>();

            beanContextFactory.RegisterBean<XmlTypeRegistry>("xmlTypeRegistry").Autowireable(typeof(IXmlTypeRegistry), typeof(IXmlTypeExtendable));

            beanContextFactory.RegisterBean<CyclicXmlReader>("cyclicXmlReader").PropertyRefs("xmlController");

            beanContextFactory.RegisterBean<CyclicXmlWriter>("cyclicXmlWriter").PropertyRefs("xmlController");

            beanContextFactory.RegisterBean<AbstractHandler>("abstractElementHandler").PropertyRef("ClassElementHandler", "classElementHandler").Template();

            beanContextFactory.RegisterBean<ArrayNameHandler>("arrayElementHandler").Parent("abstractElementHandler");
            beanContextFactory.Link("arrayElementHandler").To<INameBasedHandlerExtendable>().With("a");

            beanContextFactory.RegisterBean<EnumNameHandler>("enumElementHandler").Parent("abstractElementHandler");
            beanContextFactory.Link("enumElementHandler").To<INameBasedHandlerExtendable>().With("e");

            beanContextFactory.RegisterBean<CyclicXmlController>("xmlController").Parent("abstractElementHandler")
                .Autowireable(typeof(ITypeBasedHandlerExtendable), typeof(INameBasedHandlerExtendable));

            beanContextFactory.RegisterBean<ClassNameHandler>("classElementHandler").Parent("abstractElementHandler");
            beanContextFactory.Link("classElementHandler").To<INameBasedHandlerExtendable>().With("c");

            beanContextFactory.RegisterBean<ObjectTypeHandler>("objectElementHandler").Parent("abstractElementHandler");
            beanContextFactory.Link("objectElementHandler").To<ITypeBasedHandlerExtendable>().With(typeof(Object));

            beanContextFactory.RegisterBean<ObjRefElementHandler>("objRefTypeHandler").Parent("abstractElementHandler");
            beanContextFactory.Link("objRefTypeHandler").To<INameBasedHandlerExtendable>().With("or");

            beanContextFactory.RegisterBean<StringNameHandler>("stringElementHandler").Parent("abstractElementHandler");
            beanContextFactory.Link("stringElementHandler").To<INameBasedHandlerExtendable>().With("s");

            beanContextFactory.RegisterBean<OriWrapperElementHandler>("oriWrapperElementHandler").Parent("abstractElementHandler");
            beanContextFactory.Link("oriWrapperElementHandler").To<INameBasedHandlerExtendable>().With("ow");

            beanContextFactory.RegisterBean<NumberTypeHandler>("numberElementHandler").Parent("abstractElementHandler");
            beanContextFactory.Link("numberElementHandler").To<ITypeBasedHandlerExtendable>().With(typeof(Int64?));
            beanContextFactory.Link("numberElementHandler").To<ITypeBasedHandlerExtendable>().With(typeof(Int32?));
            beanContextFactory.Link("numberElementHandler").To<ITypeBasedHandlerExtendable>().With(typeof(Int16?));
            beanContextFactory.Link("numberElementHandler").To<ITypeBasedHandlerExtendable>().With(typeof(UInt64?));
            beanContextFactory.Link("numberElementHandler").To<ITypeBasedHandlerExtendable>().With(typeof(UInt32?));
            beanContextFactory.Link("numberElementHandler").To<ITypeBasedHandlerExtendable>().With(typeof(UInt16?));
            beanContextFactory.Link("numberElementHandler").To<ITypeBasedHandlerExtendable>().With(typeof(Double?));
            beanContextFactory.Link("numberElementHandler").To<ITypeBasedHandlerExtendable>().With(typeof(Single?));
            beanContextFactory.Link("numberElementHandler").To<ITypeBasedHandlerExtendable>().With(typeof(Byte?));
            beanContextFactory.Link("numberElementHandler").To<ITypeBasedHandlerExtendable>().With(typeof(SByte?));
            beanContextFactory.Link("numberElementHandler").To<ITypeBasedHandlerExtendable>().With(typeof(Boolean?));
            beanContextFactory.Link("numberElementHandler").To<ITypeBasedHandlerExtendable>().With(typeof(Char?));
            beanContextFactory.Link("numberElementHandler").To<ITypeBasedHandlerExtendable>().With(typeof(Int64));
            beanContextFactory.Link("numberElementHandler").To<ITypeBasedHandlerExtendable>().With(typeof(Int32));
            beanContextFactory.Link("numberElementHandler").To<ITypeBasedHandlerExtendable>().With(typeof(Int16));
            beanContextFactory.Link("numberElementHandler").To<ITypeBasedHandlerExtendable>().With(typeof(UInt64));
            beanContextFactory.Link("numberElementHandler").To<ITypeBasedHandlerExtendable>().With(typeof(UInt32));
            beanContextFactory.Link("numberElementHandler").To<ITypeBasedHandlerExtendable>().With(typeof(UInt16));
            beanContextFactory.Link("numberElementHandler").To<ITypeBasedHandlerExtendable>().With(typeof(Double));
            beanContextFactory.Link("numberElementHandler").To<ITypeBasedHandlerExtendable>().With(typeof(Single));
            beanContextFactory.Link("numberElementHandler").To<ITypeBasedHandlerExtendable>().With(typeof(Byte));
            beanContextFactory.Link("numberElementHandler").To<ITypeBasedHandlerExtendable>().With(typeof(SByte));
            beanContextFactory.Link("numberElementHandler").To<ITypeBasedHandlerExtendable>().With(typeof(Boolean));
            beanContextFactory.Link("numberElementHandler").To<ITypeBasedHandlerExtendable>().With(typeof(Char));

            beanContextFactory.RegisterBean<DateElementHandler>("dateTypeHandler").Parent("abstractElementHandler");
            beanContextFactory.Link("dateTypeHandler").To<INameBasedHandlerExtendable>().With("d");

            beanContextFactory.RegisterBean<TimeSpanElementHandler>("timeSpanTypeHandler").Parent("abstractElementHandler");
            beanContextFactory.Link("timeSpanTypeHandler").To<INameBasedHandlerExtendable>().With("timespan");

            beanContextFactory.RegisterBean<CollectionElementHandler>("collectionElementHandler").Parent("abstractElementHandler");
            beanContextFactory.Link("collectionElementHandler").To<INameBasedHandlerExtendable>().With("l");
            beanContextFactory.Link("collectionElementHandler").To<INameBasedHandlerExtendable>().With("set");

            beanContextFactory.RegisterBean<CyclicXmlHandler>("cyclicXmlHandler")
                .PropertyRefs("cyclicXmlReader", "cyclicXmlWriter")
                .Autowireable(typeof(ICyclicXmlHandler), typeof(ICyclicXmlWriter), typeof(ICyclicXmlReader));

            beanContextFactory.RegisterBean<CyclicXmlDictionary>("XmlDictionary").Autowireable<ICyclicXmlDictionary>();

            beanContextFactory.RegisterBean<XmlTransferScanner>("xmlTransferScanner");

            beanContextFactory.RegisterBean<ExtendableBean>("objectFutureHandlerExtendable")
                    .PropertyValue(ExtendableBean.P_EXTENDABLE_TYPE, typeof(IObjectFutureHandlerExtendable))
                    .PropertyValue(ExtendableBean.P_PROVIDER_TYPE, typeof(IObjectFutureHandlerRegistry))
                    .Autowireable(typeof(IObjectFutureHandlerExtendable), typeof(IObjectFutureHandlerRegistry));

            beanContextFactory.RegisterBean<ObjRefFutureHandler>("objRefFutureHandler");
            beanContextFactory.Link("objRefFutureHandler").To<IObjectFutureHandlerExtendable>().With(typeof(ObjRefFuture));

            beanContextFactory.RegisterBean<PrefetchFutureHandler>("prefetchFutureHandler");
            beanContextFactory.Link("prefetchFutureHandler").To<IObjectFutureHandlerExtendable>().With(typeof(PrefetchFuture));

            beanContextFactory.RegisterBean<CommandBuilder>("commandBuilder").Autowireable<ICommandBuilder>();

            beanContextFactory.RegisterBean<ExtendableBean>("xmlPostProcessorRegistry")
                    .PropertyValue(ExtendableBean.P_EXTENDABLE_TYPE, typeof(IXmlPostProcessorExtendable))
                    .PropertyValue(ExtendableBean.P_PROVIDER_TYPE, typeof(IXmlPostProcessorRegistry))
                    .PropertyValue("ArgumentTypes", new Type[] { typeof(String) })
                    .Autowireable(typeof(IXmlPostProcessorExtendable), typeof(IXmlPostProcessorRegistry));

            beanContextFactory.RegisterBean<MergeXmlPostProcessor>("mergeXmlPostProcessor");
            beanContextFactory.Link("mergeXmlPostProcessor").To(typeof(IXmlPostProcessorExtendable)).With("merge");
        }
    }
}
