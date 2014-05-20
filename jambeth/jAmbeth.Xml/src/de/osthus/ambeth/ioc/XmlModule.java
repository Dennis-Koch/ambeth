package de.osthus.ambeth.ioc;

import java.nio.ByteBuffer;

import de.osthus.ambeth.ioc.annotation.FrameworkModule;
import de.osthus.ambeth.ioc.config.IBeanConfiguration;
import de.osthus.ambeth.ioc.extendable.ExtendableBean;
import de.osthus.ambeth.ioc.factory.IBeanContextFactory;
import de.osthus.ambeth.objectcollector.ByteBuffer65536CollectableController;
import de.osthus.ambeth.objectcollector.ICollectableControllerExtendable;
import de.osthus.ambeth.xml.CyclicXMLHandler;
import de.osthus.ambeth.xml.CyclicXMLReader;
import de.osthus.ambeth.xml.CyclicXMLWriter;
import de.osthus.ambeth.xml.CyclicXmlController;
import de.osthus.ambeth.xml.CyclicXmlDictionary;
import de.osthus.ambeth.xml.ICyclicXMLHandler;
import de.osthus.ambeth.xml.ICyclicXmlDictionary;
import de.osthus.ambeth.xml.ICyclicXmlReader;
import de.osthus.ambeth.xml.ICyclicXmlWriter;
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
import de.osthus.ambeth.xml.typehandler.AbstractHandler;
import de.osthus.ambeth.xml.typehandler.NumberTypeHandler;
import de.osthus.ambeth.xml.typehandler.ObjectTypeHandler;

@FrameworkModule
public class XmlModule implements IInitializingModule
{
	@Override
	public void afterPropertiesSet(IBeanContextFactory beanContextFactory) throws Throwable
	{
		beanContextFactory.registerBean("xmlTypeRegistry", XmlTypeRegistry.class).autowireable(IXmlTypeRegistry.class, IXmlTypeExtendable.class);

		beanContextFactory.registerBean("cyclicXmlReader", CyclicXMLReader.class).propertyRefs("xmlController");

		beanContextFactory.registerBean("cyclicXmlWriter", CyclicXMLWriter.class).propertyRefs("xmlController");

		beanContextFactory.registerBean("abstractElementHandler", AbstractHandler.class).propertyRef("ClassElementHandler", "classElementHandler").template();

		beanContextFactory.registerBean("arrayElementHandler", ArrayNameHandler.class).parent("abstractElementHandler");
		beanContextFactory.link("arrayElementHandler").to(INameBasedHandlerExtendable.class).with("a");

		beanContextFactory.registerBean("enumElementHandler", EnumNameHandler.class).parent("abstractElementHandler");
		beanContextFactory.link("enumElementHandler").to(INameBasedHandlerExtendable.class).with("e");

		beanContextFactory.registerBean("xmlController", CyclicXmlController.class).parent("abstractElementHandler")
				.autowireable(ITypeBasedHandlerExtendable.class, INameBasedHandlerExtendable.class);

		beanContextFactory.registerBean("classElementHandler", ClassNameHandler.class).parent("abstractElementHandler");
		beanContextFactory.link("classElementHandler").to(INameBasedHandlerExtendable.class).with("c");

		beanContextFactory.registerBean("objectElementHandler", ObjectTypeHandler.class).parent("abstractElementHandler");
		beanContextFactory.link("objectElementHandler").to(ITypeBasedHandlerExtendable.class).with(Object.class);

		beanContextFactory.registerBean("objRefElementHandler", ObjRefElementHandler.class).parent("abstractElementHandler");
		beanContextFactory.link("objRefElementHandler").to(INameBasedHandlerExtendable.class).with("or");

		beanContextFactory.registerBean("stringElementHandler", StringNameHandler.class).parent("abstractElementHandler");
		beanContextFactory.link("stringElementHandler").to(INameBasedHandlerExtendable.class).with("s");

		beanContextFactory.registerBean("oriWrapperElementHandler", OriWrapperElementHandler.class).parent("abstractElementHandler");
		beanContextFactory.link("oriWrapperElementHandler").to(INameBasedHandlerExtendable.class).with("ow");

		beanContextFactory.registerBean("numberElementHandler", NumberTypeHandler.class).parent("abstractElementHandler");
		beanContextFactory.link("numberElementHandler").to(ITypeBasedHandlerExtendable.class).with(Byte.class);
		beanContextFactory.link("numberElementHandler").to(ITypeBasedHandlerExtendable.class).with(Short.class);
		beanContextFactory.link("numberElementHandler").to(ITypeBasedHandlerExtendable.class).with(Integer.class);
		beanContextFactory.link("numberElementHandler").to(ITypeBasedHandlerExtendable.class).with(Long.class);
		beanContextFactory.link("numberElementHandler").to(ITypeBasedHandlerExtendable.class).with(Float.class);
		beanContextFactory.link("numberElementHandler").to(ITypeBasedHandlerExtendable.class).with(Double.class);
		beanContextFactory.link("numberElementHandler").to(ITypeBasedHandlerExtendable.class).with(Boolean.class);
		beanContextFactory.link("numberElementHandler").to(ITypeBasedHandlerExtendable.class).with(Character.class);
		beanContextFactory.link("numberElementHandler").to(ITypeBasedHandlerExtendable.class).with(Byte.TYPE);
		beanContextFactory.link("numberElementHandler").to(ITypeBasedHandlerExtendable.class).with(Short.TYPE);
		beanContextFactory.link("numberElementHandler").to(ITypeBasedHandlerExtendable.class).with(Integer.TYPE);
		beanContextFactory.link("numberElementHandler").to(ITypeBasedHandlerExtendable.class).with(Long.TYPE);
		beanContextFactory.link("numberElementHandler").to(ITypeBasedHandlerExtendable.class).with(Float.TYPE);
		beanContextFactory.link("numberElementHandler").to(ITypeBasedHandlerExtendable.class).with(Double.TYPE);
		beanContextFactory.link("numberElementHandler").to(ITypeBasedHandlerExtendable.class).with(Boolean.TYPE);
		beanContextFactory.link("numberElementHandler").to(ITypeBasedHandlerExtendable.class).with(Character.TYPE);

		beanContextFactory.registerBean("dateTypeHandler", DateElementHandler.class).parent("abstractElementHandler");
		beanContextFactory.link("dateTypeHandler").to(INameBasedHandlerExtendable.class).with("d");

		beanContextFactory.registerBean("collectionElementHandler", CollectionElementHandler.class).parent("abstractElementHandler");
		beanContextFactory.link("collectionElementHandler").to(INameBasedHandlerExtendable.class).with("l");
		beanContextFactory.link("collectionElementHandler").to(INameBasedHandlerExtendable.class).with("set");

		beanContextFactory.registerBean("cyclicXmlHandler", CyclicXMLHandler.class).propertyRefs("cyclicXmlReader", "cyclicXmlWriter")
				.autowireable(ICyclicXMLHandler.class, ICyclicXmlWriter.class, ICyclicXmlReader.class);

		beanContextFactory.registerBean("xmlTransferScanner", XmlTransferScanner.class);

		beanContextFactory.registerBean("xmlDictionary", CyclicXmlDictionary.class).autowireable(ICyclicXmlDictionary.class);

		ExtendableBean.registerExtendableBean(beanContextFactory, "objectFutureHandlerExtendable", IObjectFutureHandlerRegistry.class,
				IObjectFutureHandlerExtendable.class);

		beanContextFactory.registerBean("objRefFutureHandler", ObjRefFutureHandler.class);
		beanContextFactory.link("objRefFutureHandler").to(IObjectFutureHandlerExtendable.class).with(ObjRefFuture.class);

		beanContextFactory.registerBean("prefetchFutureHandler", PrefetchFutureHandler.class);
		beanContextFactory.link("prefetchFutureHandler").to(IObjectFutureHandlerExtendable.class).with(PrefetchFuture.class);

		beanContextFactory.registerBean("commandBuilder", CommandBuilder.class).autowireable(ICommandBuilder.class);

		beanContextFactory.registerBean("xmlPostProcessorRegistry", ExtendableBean.class)
				.propertyValue(ExtendableBean.P_EXTENDABLE_TYPE, IXmlPostProcessorExtendable.class)
				.propertyValue(ExtendableBean.P_PROVIDER_TYPE, IXmlPostProcessorRegistry.class).propertyValue("ArgumentTypes", new Class<?>[] { String.class })
				.autowireable(IXmlPostProcessorExtendable.class, IXmlPostProcessorRegistry.class);

		beanContextFactory.registerBean("mergeXmlPostProcessor", MergeXmlPostProcessor.class);
		beanContextFactory.link("mergeXmlPostProcessor").to(IXmlPostProcessorExtendable.class).with("merge");

		IBeanConfiguration byteBufferCC = beanContextFactory.registerAnonymousBean(ByteBuffer65536CollectableController.class);
		beanContextFactory.link(byteBufferCC).to(ICollectableControllerExtendable.class).with(ByteBuffer.class);
	}
}
