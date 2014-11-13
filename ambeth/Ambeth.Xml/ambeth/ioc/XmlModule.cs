using De.Osthus.Ambeth.Ioc.Annotation;
using De.Osthus.Ambeth.Ioc.Config;
using De.Osthus.Ambeth.Ioc.Extendable;
using De.Osthus.Ambeth.Ioc.Factory;
using De.Osthus.Ambeth.Log;
using De.Osthus.Ambeth.Xml;
using De.Osthus.Ambeth.Xml.Namehandler;
using De.Osthus.Ambeth.Xml.Pending;
using De.Osthus.Ambeth.Xml.PostProcess;
using De.Osthus.Ambeth.Xml.Simple;
using De.Osthus.Ambeth.Xml.Typehandler;
using System;

namespace De.Osthus.Ambeth.Ioc
{
    [FrameworkModule]
    public class XmlModule : IInitializingModule
    {
        public const String CYCLIC_XML_HANDLER = "cyclicXmlHandler";

        public const String SIMPLE_XML_HANDLER = "simpleXmlHandler";

        [LogInstance]
        public ILogger Log { private get; set; }

        public virtual void AfterPropertiesSet(IBeanContextFactory beanContextFactory)
        {
            {
                IBeanConfiguration cyclicXmlControllerBC = beanContextFactory.RegisterBean(typeof(CyclicXmlController)).Parent("abstractElementHandler");

                IBeanConfiguration cyclicXmlReaderBC = beanContextFactory.RegisterBean(typeof(CyclicXmlReader)).PropertyRefs(cyclicXmlControllerBC);

                IBeanConfiguration cyclicXmlWriterBC = beanContextFactory.RegisterBean(typeof(CyclicXmlWriter)).PropertyRefs(cyclicXmlControllerBC);

                beanContextFactory.RegisterBean(CYCLIC_XML_HANDLER, typeof(CyclicXmlHandler)).PropertyRefs(cyclicXmlReaderBC, cyclicXmlWriterBC,
                        cyclicXmlControllerBC);
            }
            {
                IBeanConfiguration simpleXmlControllerBC = beanContextFactory.RegisterBean(typeof(SimpleXmlController));

                IBeanConfiguration simpleXmlReaderBC = beanContextFactory.RegisterBean(typeof(SimpleXmlReader)).PropertyRefs(simpleXmlControllerBC);

                IBeanConfiguration simpleXmlWriterBC = beanContextFactory.RegisterBean(typeof(SimpleXmlWriter)).PropertyRefs(simpleXmlControllerBC);

                beanContextFactory.RegisterBean(SIMPLE_XML_HANDLER, typeof(CyclicXmlHandler)).PropertyRefs(simpleXmlReaderBC, simpleXmlWriterBC,
                        simpleXmlControllerBC);
            }

            beanContextFactory.RegisterBean(typeof(XmlTypeRegistry)).Autowireable(typeof(IXmlTypeRegistry), typeof(IXmlTypeExtendable));

            beanContextFactory.RegisterBean(typeof(CommandBuilder)).Autowireable(typeof(ICommandBuilder));

            IBeanConfiguration classElementHandlerBC = beanContextFactory.RegisterBean(typeof(ClassNameHandler)).Parent("abstractElementHandler");
            beanContextFactory.Link(classElementHandlerBC).To(CYCLIC_XML_HANDLER, typeof(INameBasedHandlerExtendable)).With("c");

            IBeanConfiguration objectElementHandlerBC = beanContextFactory.RegisterBean(typeof(ObjectTypeHandler)).Parent("abstractElementHandler");
            beanContextFactory.Link(objectElementHandlerBC).To(CYCLIC_XML_HANDLER, typeof(ITypeBasedHandlerExtendable)).With(typeof(Object));

            IBeanConfiguration objRefElementHandlerBC = beanContextFactory.RegisterBean(typeof(ObjRefElementHandler)).Parent("abstractElementHandler");
            beanContextFactory.Link(objRefElementHandlerBC).To(CYCLIC_XML_HANDLER, typeof(INameBasedHandlerExtendable)).With("or");

            IBeanConfiguration stringElementHandlerBC = beanContextFactory.RegisterBean(typeof(StringNameHandler)).Parent("abstractElementHandler");
            beanContextFactory.Link(stringElementHandlerBC).To(CYCLIC_XML_HANDLER, typeof(INameBasedHandlerExtendable)).With("s");

            IBeanConfiguration oriWrapperElementHandlerBC = beanContextFactory.RegisterBean(typeof(ObjRefWrapperElementHandler)).Parent(
                    "abstractElementHandler");
            beanContextFactory.Link(oriWrapperElementHandlerBC).To(CYCLIC_XML_HANDLER, typeof(INameBasedHandlerExtendable)).With("ow");

            IBeanConfiguration numberElementHandlerBC = beanContextFactory.RegisterBean(typeof(NumberTypeHandler)).Parent("abstractElementHandler");
            beanContextFactory.Link(numberElementHandlerBC).To<ITypeBasedHandlerExtendable>(CYCLIC_XML_HANDLER).With(typeof(Int64?));
            beanContextFactory.Link(numberElementHandlerBC).To<ITypeBasedHandlerExtendable>(CYCLIC_XML_HANDLER).With(typeof(Int32?));
            beanContextFactory.Link(numberElementHandlerBC).To<ITypeBasedHandlerExtendable>(CYCLIC_XML_HANDLER).With(typeof(Int16?));
            beanContextFactory.Link(numberElementHandlerBC).To<ITypeBasedHandlerExtendable>(CYCLIC_XML_HANDLER).With(typeof(UInt64?));
            beanContextFactory.Link(numberElementHandlerBC).To<ITypeBasedHandlerExtendable>(CYCLIC_XML_HANDLER).With(typeof(UInt32?));
            beanContextFactory.Link(numberElementHandlerBC).To<ITypeBasedHandlerExtendable>(CYCLIC_XML_HANDLER).With(typeof(UInt16?));
            beanContextFactory.Link(numberElementHandlerBC).To<ITypeBasedHandlerExtendable>(CYCLIC_XML_HANDLER).With(typeof(Double?));
            beanContextFactory.Link(numberElementHandlerBC).To<ITypeBasedHandlerExtendable>(CYCLIC_XML_HANDLER).With(typeof(Single?));
            beanContextFactory.Link(numberElementHandlerBC).To<ITypeBasedHandlerExtendable>(CYCLIC_XML_HANDLER).With(typeof(Byte?));
            beanContextFactory.Link(numberElementHandlerBC).To<ITypeBasedHandlerExtendable>(CYCLIC_XML_HANDLER).With(typeof(SByte?));
            beanContextFactory.Link(numberElementHandlerBC).To<ITypeBasedHandlerExtendable>(CYCLIC_XML_HANDLER).With(typeof(Boolean?));
            beanContextFactory.Link(numberElementHandlerBC).To<ITypeBasedHandlerExtendable>(CYCLIC_XML_HANDLER).With(typeof(Char?));
            beanContextFactory.Link(numberElementHandlerBC).To<ITypeBasedHandlerExtendable>(CYCLIC_XML_HANDLER).With(typeof(Int64));
            beanContextFactory.Link(numberElementHandlerBC).To<ITypeBasedHandlerExtendable>(CYCLIC_XML_HANDLER).With(typeof(Int32));
            beanContextFactory.Link(numberElementHandlerBC).To<ITypeBasedHandlerExtendable>(CYCLIC_XML_HANDLER).With(typeof(Int16));
            beanContextFactory.Link(numberElementHandlerBC).To<ITypeBasedHandlerExtendable>(CYCLIC_XML_HANDLER).With(typeof(UInt64));
            beanContextFactory.Link(numberElementHandlerBC).To<ITypeBasedHandlerExtendable>(CYCLIC_XML_HANDLER).With(typeof(UInt32));
            beanContextFactory.Link(numberElementHandlerBC).To<ITypeBasedHandlerExtendable>(CYCLIC_XML_HANDLER).With(typeof(UInt16));
            beanContextFactory.Link(numberElementHandlerBC).To<ITypeBasedHandlerExtendable>(CYCLIC_XML_HANDLER).With(typeof(Double));
            beanContextFactory.Link(numberElementHandlerBC).To<ITypeBasedHandlerExtendable>(CYCLIC_XML_HANDLER).With(typeof(Single));
            beanContextFactory.Link(numberElementHandlerBC).To<ITypeBasedHandlerExtendable>(CYCLIC_XML_HANDLER).With(typeof(Byte));
            beanContextFactory.Link(numberElementHandlerBC).To<ITypeBasedHandlerExtendable>(CYCLIC_XML_HANDLER).With(typeof(SByte));
            beanContextFactory.Link(numberElementHandlerBC).To<ITypeBasedHandlerExtendable>(CYCLIC_XML_HANDLER).With(typeof(Boolean));
            beanContextFactory.Link(numberElementHandlerBC).To<ITypeBasedHandlerExtendable>(CYCLIC_XML_HANDLER).With(typeof(Char));


            IBeanConfiguration dateTypeHandlerBC = beanContextFactory.RegisterBean(typeof(DateElementHandler)).Parent("abstractElementHandler");
            beanContextFactory.Link(dateTypeHandlerBC).To(CYCLIC_XML_HANDLER, typeof(INameBasedHandlerExtendable)).With("d");

            IBeanConfiguration collectionElementHandlerBC = beanContextFactory.RegisterBean(typeof(CollectionElementHandler)).Parent(
                    "abstractElementHandler");
            beanContextFactory.Link(collectionElementHandlerBC).To(CYCLIC_XML_HANDLER, typeof(INameBasedHandlerExtendable)).With("l");
            beanContextFactory.Link(collectionElementHandlerBC).To(CYCLIC_XML_HANDLER, typeof(INameBasedHandlerExtendable)).With("set");

            beanContextFactory.RegisterBean("abstractElementHandler", typeof(AbstractHandler)).PropertyRef("ClassElementHandler", classElementHandlerBC).Template();

            IBeanConfiguration arrayElementHandlerBC = beanContextFactory.RegisterBean(typeof(ArrayNameHandler)).Parent("abstractElementHandler");
            beanContextFactory.Link(arrayElementHandlerBC).To(CYCLIC_XML_HANDLER, typeof(INameBasedHandlerExtendable)).With("a");

            IBeanConfiguration enumElementHandlerBC = beanContextFactory.RegisterBean(typeof(EnumNameHandler)).Parent("abstractElementHandler");
            beanContextFactory.Link(enumElementHandlerBC).To(CYCLIC_XML_HANDLER, typeof(INameBasedHandlerExtendable)).With("e");

            IBeanConfiguration timeSpanElementHandlerBC = beanContextFactory.RegisterBean(typeof(TimeSpanElementHandler)).Parent("abstractElementHandler");
            beanContextFactory.Link(timeSpanElementHandlerBC).To(CYCLIC_XML_HANDLER, typeof(INameBasedHandlerExtendable)).With("t");

            beanContextFactory.RegisterBean("xmlTransferScanner", typeof(XmlTransferScanner));

            beanContextFactory.RegisterBean("xmlDictionary", typeof(CyclicXmlDictionary)).Autowireable(typeof(ICyclicXmlDictionary));

            ExtendableBean.RegisterExtendableBean(beanContextFactory, "objectFutureHandlerExtendable", typeof(IObjectFutureHandlerRegistry),
                    typeof(IObjectFutureHandlerExtendable));

            beanContextFactory.RegisterBean("objRefFutureHandler", typeof(ObjRefFutureHandler));
            beanContextFactory.Link("objRefFutureHandler").To(typeof(IObjectFutureHandlerExtendable)).With(typeof(ObjRefFuture));

            beanContextFactory.RegisterBean("prefetchFutureHandler", typeof(PrefetchFutureHandler));
            beanContextFactory.Link("prefetchFutureHandler").To(typeof(IObjectFutureHandlerExtendable)).With(typeof(PrefetchFuture));

            ExtendableBean.RegisterExtendableBean(beanContextFactory, "xmlPostProcessorRegistry", typeof(IXmlPostProcessorRegistry),
                    typeof(IXmlPostProcessorExtendable)).PropertyValue("ArgumentTypes", new Type[] { typeof(String) });

            beanContextFactory.RegisterBean("mergeXmlPostProcessor", typeof(MergeXmlPostProcessor));
            beanContextFactory.Link("mergeXmlPostProcessor").To(typeof(IXmlPostProcessorExtendable)).With("merge");
        }
    }
}
