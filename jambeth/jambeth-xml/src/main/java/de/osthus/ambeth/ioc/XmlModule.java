package de.osthus.ambeth.ioc;

import java.nio.ByteBuffer;

import de.osthus.ambeth.ioc.annotation.FrameworkModule;
import de.osthus.ambeth.ioc.config.IBeanConfiguration;
import de.osthus.ambeth.ioc.extendable.ExtendableBean;
import de.osthus.ambeth.ioc.factory.IBeanContextFactory;
import de.osthus.ambeth.objectcollector.ByteBuffer65536CollectableController;
import de.osthus.ambeth.objectcollector.ICollectableControllerExtendable;
import de.osthus.ambeth.xml.CyclicXmlController;
import de.osthus.ambeth.xml.CyclicXmlDictionary;
import de.osthus.ambeth.xml.CyclicXmlHandler;
import de.osthus.ambeth.xml.CyclicXmlReader;
import de.osthus.ambeth.xml.CyclicXmlWriter;
import de.osthus.ambeth.xml.ICyclicXmlDictionary;
import de.osthus.ambeth.xml.INameBasedHandlerExtendable;
import de.osthus.ambeth.xml.ITypeBasedHandlerExtendable;
import de.osthus.ambeth.xml.IXmlTypeExtendable;
import de.osthus.ambeth.xml.IXmlTypeRegistry;
import de.osthus.ambeth.xml.XmlTransferScanner;
import de.osthus.ambeth.xml.XmlTypeRegistry;
import de.osthus.ambeth.xml.namehandler.ArrayNameHandler;
import de.osthus.ambeth.xml.namehandler.ClassNameHandler;
import de.osthus.ambeth.xml.namehandler.CollectionElementHandler;
import de.osthus.ambeth.xml.namehandler.DateElementHandler;
import de.osthus.ambeth.xml.namehandler.EnumNameHandler;
import de.osthus.ambeth.xml.namehandler.ObjRefElementHandler;
import de.osthus.ambeth.xml.namehandler.OriWrapperElementHandler;
import de.osthus.ambeth.xml.namehandler.StringNameHandler;
import de.osthus.ambeth.xml.pending.CommandBuilder;
import de.osthus.ambeth.xml.pending.ICommandBuilder;
import de.osthus.ambeth.xml.pending.IObjectFutureHandlerExtendable;
import de.osthus.ambeth.xml.pending.IObjectFutureHandlerRegistry;
import de.osthus.ambeth.xml.pending.ObjRefFuture;
import de.osthus.ambeth.xml.pending.ObjRefFutureHandler;
import de.osthus.ambeth.xml.pending.PrefetchFuture;
import de.osthus.ambeth.xml.pending.PrefetchFutureHandler;
import de.osthus.ambeth.xml.postprocess.IXmlPostProcessorExtendable;
import de.osthus.ambeth.xml.postprocess.IXmlPostProcessorRegistry;
import de.osthus.ambeth.xml.postprocess.merge.MergeXmlPostProcessor;
import de.osthus.ambeth.xml.simple.SimpleXmlController;
import de.osthus.ambeth.xml.simple.SimpleXmlReader;
import de.osthus.ambeth.xml.simple.SimpleXmlWriter;
import de.osthus.ambeth.xml.typehandler.AbstractHandler;
import de.osthus.ambeth.xml.typehandler.NumberTypeHandler;
import de.osthus.ambeth.xml.typehandler.ObjectTypeHandler;

@FrameworkModule
public class XmlModule implements IInitializingModule
{
	public static final String CYCLIC_XML_HANDLER = "cyclicXmlHandler";

	public static final String SIMPLE_XML_HANDLER = "simpleXmlHandler";

	@Override
	public void afterPropertiesSet(IBeanContextFactory beanContextFactory) throws Throwable
	{
		{
			IBeanConfiguration cyclicXmlControllerBC = beanContextFactory.registerAnonymousBean(CyclicXmlController.class).parent("abstractElementHandler");

			IBeanConfiguration cyclicXmlReaderBC = beanContextFactory.registerAnonymousBean(CyclicXmlReader.class).propertyRefs(cyclicXmlControllerBC);

			IBeanConfiguration cyclicXmlWriterBC = beanContextFactory.registerAnonymousBean(CyclicXmlWriter.class).propertyRefs(cyclicXmlControllerBC);

			beanContextFactory.registerBean(CYCLIC_XML_HANDLER, CyclicXmlHandler.class).propertyRefs(cyclicXmlReaderBC, cyclicXmlWriterBC,
					cyclicXmlControllerBC);
		}
		{
			IBeanConfiguration simpleXmlControllerBC = beanContextFactory.registerAnonymousBean(SimpleXmlController.class);

			IBeanConfiguration simpleXmlReaderBC = beanContextFactory.registerAnonymousBean(SimpleXmlReader.class).propertyRefs(simpleXmlControllerBC);

			IBeanConfiguration simpleXmlWriterBC = beanContextFactory.registerAnonymousBean(SimpleXmlWriter.class).propertyRefs(simpleXmlControllerBC);

			beanContextFactory.registerBean(SIMPLE_XML_HANDLER, CyclicXmlHandler.class).propertyRefs(simpleXmlReaderBC, simpleXmlWriterBC,
					simpleXmlControllerBC);
		}

		beanContextFactory.registerAnonymousBean(XmlTypeRegistry.class).autowireable(IXmlTypeRegistry.class, IXmlTypeExtendable.class);

		beanContextFactory.registerAnonymousBean(CommandBuilder.class).autowireable(ICommandBuilder.class);

		IBeanConfiguration classElementHandlerBC = beanContextFactory.registerAnonymousBean(ClassNameHandler.class).parent("abstractElementHandler");
		beanContextFactory.link(classElementHandlerBC).to(CYCLIC_XML_HANDLER, INameBasedHandlerExtendable.class).with("c");

		IBeanConfiguration objectElementHandlerBC = beanContextFactory.registerAnonymousBean(ObjectTypeHandler.class).parent("abstractElementHandler");
		beanContextFactory.link(objectElementHandlerBC).to(CYCLIC_XML_HANDLER, ITypeBasedHandlerExtendable.class).with(Object.class);

		IBeanConfiguration objRefElementHandlerBC = beanContextFactory.registerAnonymousBean(ObjRefElementHandler.class).parent("abstractElementHandler");
		beanContextFactory.link(objRefElementHandlerBC).to(CYCLIC_XML_HANDLER, INameBasedHandlerExtendable.class).with("or");

		IBeanConfiguration stringElementHandlerBC = beanContextFactory.registerAnonymousBean(StringNameHandler.class).parent("abstractElementHandler");
		beanContextFactory.link(stringElementHandlerBC).to(CYCLIC_XML_HANDLER, INameBasedHandlerExtendable.class).with("s");

		IBeanConfiguration oriWrapperElementHandlerBC = beanContextFactory.registerAnonymousBean(OriWrapperElementHandler.class).parent(
				"abstractElementHandler");
		beanContextFactory.link(oriWrapperElementHandlerBC).to(CYCLIC_XML_HANDLER, INameBasedHandlerExtendable.class).with("ow");

		IBeanConfiguration numberElementHandlerBC = beanContextFactory.registerAnonymousBean(NumberTypeHandler.class).parent("abstractElementHandler");
		beanContextFactory.link(numberElementHandlerBC).to(CYCLIC_XML_HANDLER, ITypeBasedHandlerExtendable.class).with(Byte.class);
		beanContextFactory.link(numberElementHandlerBC).to(CYCLIC_XML_HANDLER, ITypeBasedHandlerExtendable.class).with(Short.class);
		beanContextFactory.link(numberElementHandlerBC).to(CYCLIC_XML_HANDLER, ITypeBasedHandlerExtendable.class).with(Integer.class);
		beanContextFactory.link(numberElementHandlerBC).to(CYCLIC_XML_HANDLER, ITypeBasedHandlerExtendable.class).with(Long.class);
		beanContextFactory.link(numberElementHandlerBC).to(CYCLIC_XML_HANDLER, ITypeBasedHandlerExtendable.class).with(Float.class);
		beanContextFactory.link(numberElementHandlerBC).to(CYCLIC_XML_HANDLER, ITypeBasedHandlerExtendable.class).with(Double.class);
		beanContextFactory.link(numberElementHandlerBC).to(CYCLIC_XML_HANDLER, ITypeBasedHandlerExtendable.class).with(Boolean.class);
		beanContextFactory.link(numberElementHandlerBC).to(CYCLIC_XML_HANDLER, ITypeBasedHandlerExtendable.class).with(Character.class);
		beanContextFactory.link(numberElementHandlerBC).to(CYCLIC_XML_HANDLER, ITypeBasedHandlerExtendable.class).with(Byte.TYPE);
		beanContextFactory.link(numberElementHandlerBC).to(CYCLIC_XML_HANDLER, ITypeBasedHandlerExtendable.class).with(Short.TYPE);
		beanContextFactory.link(numberElementHandlerBC).to(CYCLIC_XML_HANDLER, ITypeBasedHandlerExtendable.class).with(Integer.TYPE);
		beanContextFactory.link(numberElementHandlerBC).to(CYCLIC_XML_HANDLER, ITypeBasedHandlerExtendable.class).with(Long.TYPE);
		beanContextFactory.link(numberElementHandlerBC).to(CYCLIC_XML_HANDLER, ITypeBasedHandlerExtendable.class).with(Float.TYPE);
		beanContextFactory.link(numberElementHandlerBC).to(CYCLIC_XML_HANDLER, ITypeBasedHandlerExtendable.class).with(Double.TYPE);
		beanContextFactory.link(numberElementHandlerBC).to(CYCLIC_XML_HANDLER, ITypeBasedHandlerExtendable.class).with(Boolean.TYPE);
		beanContextFactory.link(numberElementHandlerBC).to(CYCLIC_XML_HANDLER, ITypeBasedHandlerExtendable.class).with(Character.TYPE);

		IBeanConfiguration dateTypeHandlerBC = beanContextFactory.registerAnonymousBean(DateElementHandler.class).parent("abstractElementHandler");
		beanContextFactory.link(dateTypeHandlerBC).to(CYCLIC_XML_HANDLER, INameBasedHandlerExtendable.class).with("d");

		IBeanConfiguration collectionElementHandlerBC = beanContextFactory.registerAnonymousBean(CollectionElementHandler.class).parent(
				"abstractElementHandler");
		beanContextFactory.link(collectionElementHandlerBC).to(CYCLIC_XML_HANDLER, INameBasedHandlerExtendable.class).with("l");
		beanContextFactory.link(collectionElementHandlerBC).to(CYCLIC_XML_HANDLER, INameBasedHandlerExtendable.class).with("set");

		beanContextFactory.registerBean("abstractElementHandler", AbstractHandler.class).propertyRef("ClassElementHandler", classElementHandlerBC).template();

		IBeanConfiguration arrayElementHandlerBC = beanContextFactory.registerAnonymousBean(ArrayNameHandler.class).parent("abstractElementHandler");
		beanContextFactory.link(arrayElementHandlerBC).to(CYCLIC_XML_HANDLER, INameBasedHandlerExtendable.class).with("a");

		IBeanConfiguration enumElementHandlerBC = beanContextFactory.registerAnonymousBean(EnumNameHandler.class).parent("abstractElementHandler");
		beanContextFactory.link(enumElementHandlerBC).to(CYCLIC_XML_HANDLER, INameBasedHandlerExtendable.class).with("e");

		beanContextFactory.registerBean("xmlTransferScanner", XmlTransferScanner.class);

		beanContextFactory.registerBean("xmlDictionary", CyclicXmlDictionary.class).autowireable(ICyclicXmlDictionary.class);

		ExtendableBean.registerExtendableBean(beanContextFactory, "objectFutureHandlerExtendable", IObjectFutureHandlerRegistry.class,
				IObjectFutureHandlerExtendable.class);

		beanContextFactory.registerBean("objRefFutureHandler", ObjRefFutureHandler.class);
		beanContextFactory.link("objRefFutureHandler").to(IObjectFutureHandlerExtendable.class).with(ObjRefFuture.class);

		beanContextFactory.registerBean("prefetchFutureHandler", PrefetchFutureHandler.class);
		beanContextFactory.link("prefetchFutureHandler").to(IObjectFutureHandlerExtendable.class).with(PrefetchFuture.class);

		ExtendableBean.registerExtendableBean(beanContextFactory, "xmlPostProcessorRegistry", IXmlPostProcessorRegistry.class,
				IXmlPostProcessorExtendable.class).propertyValue("ArgumentTypes", new Class<?>[] { String.class });

		beanContextFactory.registerBean("mergeXmlPostProcessor", MergeXmlPostProcessor.class);
		beanContextFactory.link("mergeXmlPostProcessor").to(IXmlPostProcessorExtendable.class).with("merge");

		IBeanConfiguration byteBufferCC = beanContextFactory.registerAnonymousBean(ByteBuffer65536CollectableController.class);
		beanContextFactory.link(byteBufferCC).to(ICollectableControllerExtendable.class).with(ByteBuffer.class);
	}
}
