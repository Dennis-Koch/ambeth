package com.koch.ambeth.xml.ioc;

/*-
 * #%L
 * jambeth-xml
 * %%
 * Copyright (C) 2017 Koch Softwaredevelopment
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

     http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
 * #L%
 */

import java.net.InetAddress;
import java.nio.ByteBuffer;
import java.time.Instant;
import java.util.regex.Pattern;

import com.koch.ambeth.ioc.IInitializingModule;
import com.koch.ambeth.ioc.annotation.FrameworkModule;
import com.koch.ambeth.ioc.config.IBeanConfiguration;
import com.koch.ambeth.ioc.extendable.ExtendableBean;
import com.koch.ambeth.ioc.factory.IBeanContextFactory;
import com.koch.ambeth.merge.ICUDResultPrinter;
import com.koch.ambeth.util.objectcollector.ByteBuffer65536CollectableController;
import com.koch.ambeth.util.objectcollector.ICollectableControllerExtendable;
import com.koch.ambeth.xml.CyclicXmlController;
import com.koch.ambeth.xml.CyclicXmlDictionary;
import com.koch.ambeth.xml.CyclicXmlHandler;
import com.koch.ambeth.xml.CyclicXmlReader;
import com.koch.ambeth.xml.CyclicXmlWriter;
import com.koch.ambeth.xml.ICyclicXmlDictionary;
import com.koch.ambeth.xml.INameBasedHandlerExtendable;
import com.koch.ambeth.xml.ITypeBasedHandlerExtendable;
import com.koch.ambeth.xml.IXmlTypeExtendable;
import com.koch.ambeth.xml.IXmlTypeRegistry;
import com.koch.ambeth.xml.XmlTransferScanner;
import com.koch.ambeth.xml.XmlTypeRegistry;
import com.koch.ambeth.xml.merge.CUDResultPrinter;
import com.koch.ambeth.xml.namehandler.ArrayNameHandler;
import com.koch.ambeth.xml.namehandler.ClassNameHandler;
import com.koch.ambeth.xml.namehandler.CollectionElementHandler;
import com.koch.ambeth.xml.namehandler.DateElementHandler;
import com.koch.ambeth.xml.namehandler.EnumNameHandler;
import com.koch.ambeth.xml.namehandler.ObjRefElementHandler;
import com.koch.ambeth.xml.namehandler.ObjRefWrapperElementHandler;
import com.koch.ambeth.xml.namehandler.StringNameHandler;
import com.koch.ambeth.xml.namehandler.TimeSpanElementHandler;
import com.koch.ambeth.xml.pending.CommandBuilder;
import com.koch.ambeth.xml.pending.ICommandBuilder;
import com.koch.ambeth.xml.pending.IObjectFutureHandlerExtendable;
import com.koch.ambeth.xml.pending.IObjectFutureHandlerRegistry;
import com.koch.ambeth.xml.pending.ObjRefFuture;
import com.koch.ambeth.xml.pending.ObjRefFutureHandler;
import com.koch.ambeth.xml.pending.PrefetchFuture;
import com.koch.ambeth.xml.pending.PrefetchFutureHandler;
import com.koch.ambeth.xml.postprocess.IXmlPostProcessorExtendable;
import com.koch.ambeth.xml.postprocess.IXmlPostProcessorRegistry;
import com.koch.ambeth.xml.postprocess.merge.MergeXmlPostProcessor;
import com.koch.ambeth.xml.simple.SimpleXmlController;
import com.koch.ambeth.xml.simple.SimpleXmlReader;
import com.koch.ambeth.xml.simple.SimpleXmlWriter;
import com.koch.ambeth.xml.typehandler.AbstractHandler;
import com.koch.ambeth.xml.typehandler.InetAddressTypeHandler;
import com.koch.ambeth.xml.typehandler.InstantTypeHandler;
import com.koch.ambeth.xml.typehandler.NumberTypeHandler;
import com.koch.ambeth.xml.typehandler.ObjectTypeHandler;
import com.koch.ambeth.xml.typehandler.PatternTypeHandler;

@FrameworkModule
public class XmlModule implements IInitializingModule {
	public static final String CYCLIC_XML_HANDLER = "cyclicXmlHandler";

	public static final String SIMPLE_XML_HANDLER = "simpleXmlHandler";

	@Override
	public void afterPropertiesSet(IBeanContextFactory beanContextFactory) throws Throwable {
		{
			IBeanConfiguration cyclicXmlControllerBC = beanContextFactory
					.registerBean(CyclicXmlController.class).parent("abstractElementHandler");

			IBeanConfiguration cyclicXmlReaderBC = beanContextFactory.registerBean(CyclicXmlReader.class)
					.propertyRefs(cyclicXmlControllerBC);

			IBeanConfiguration cyclicXmlWriterBC = beanContextFactory.registerBean(CyclicXmlWriter.class)
					.propertyRefs(cyclicXmlControllerBC);

			beanContextFactory.registerBean(CYCLIC_XML_HANDLER, CyclicXmlHandler.class)
					.propertyRefs(cyclicXmlReaderBC, cyclicXmlWriterBC, cyclicXmlControllerBC);
		}
		{
			IBeanConfiguration simpleXmlControllerBC =
					beanContextFactory.registerBean(SimpleXmlController.class);

			IBeanConfiguration simpleXmlReaderBC = beanContextFactory.registerBean(SimpleXmlReader.class)
					.propertyRefs(simpleXmlControllerBC);

			IBeanConfiguration simpleXmlWriterBC = beanContextFactory.registerBean(SimpleXmlWriter.class)
					.propertyRefs(simpleXmlControllerBC);

			beanContextFactory.registerBean(SIMPLE_XML_HANDLER, CyclicXmlHandler.class)
					.propertyRefs(simpleXmlReaderBC, simpleXmlWriterBC, simpleXmlControllerBC);
		}

		beanContextFactory.registerBean(XmlTypeRegistry.class).autowireable(IXmlTypeRegistry.class,
				IXmlTypeExtendable.class);

		beanContextFactory.registerBean(CommandBuilder.class).autowireable(ICommandBuilder.class);

		IBeanConfiguration classElementHandlerBC =
				beanContextFactory.registerBean(ClassNameHandler.class).parent("abstractElementHandler");
		beanContextFactory.link(classElementHandlerBC)
				.to(CYCLIC_XML_HANDLER, INameBasedHandlerExtendable.class).with("c");

		IBeanConfiguration objectElementHandlerBC =
				beanContextFactory.registerBean(ObjectTypeHandler.class).parent("abstractElementHandler");
		beanContextFactory.link(objectElementHandlerBC)
				.to(CYCLIC_XML_HANDLER, ITypeBasedHandlerExtendable.class).with(Object.class);

		IBeanConfiguration instantTypeHandlerBC =
				beanContextFactory.registerBean(InstantTypeHandler.class).parent("abstractElementHandler");
		beanContextFactory.link(instantTypeHandlerBC)
				.to(CYCLIC_XML_HANDLER, ITypeBasedHandlerExtendable.class).with(Instant.class);

		IBeanConfiguration patternTypeHandlerBC =
				beanContextFactory.registerBean(PatternTypeHandler.class).parent("abstractElementHandler");
		beanContextFactory.link(patternTypeHandlerBC)
				.to(CYCLIC_XML_HANDLER, ITypeBasedHandlerExtendable.class).with(Pattern.class);

		IBeanConfiguration inetAddressTypeHandlerBC =
				beanContextFactory.registerBean(InetAddressTypeHandler.class)
						.parent("abstractElementHandler");
		beanContextFactory.link(inetAddressTypeHandlerBC)
				.to(CYCLIC_XML_HANDLER, ITypeBasedHandlerExtendable.class).with(InetAddress.class);

		IBeanConfiguration objRefElementHandlerBC = beanContextFactory
				.registerBean(ObjRefElementHandler.class).parent("abstractElementHandler");
		beanContextFactory.link(objRefElementHandlerBC)
				.to(CYCLIC_XML_HANDLER, INameBasedHandlerExtendable.class).with("or");

		IBeanConfiguration stringElementHandlerBC =
				beanContextFactory.registerBean(StringNameHandler.class).parent("abstractElementHandler");
		beanContextFactory.link(stringElementHandlerBC)
				.to(CYCLIC_XML_HANDLER, INameBasedHandlerExtendable.class).with("s");

		IBeanConfiguration oriWrapperElementHandlerBC = beanContextFactory
				.registerBean(ObjRefWrapperElementHandler.class).parent("abstractElementHandler");
		beanContextFactory.link(oriWrapperElementHandlerBC)
				.to(CYCLIC_XML_HANDLER, INameBasedHandlerExtendable.class).with("ow");

		IBeanConfiguration numberElementHandlerBC =
				beanContextFactory.registerBean(NumberTypeHandler.class).parent("abstractElementHandler");
		beanContextFactory.link(numberElementHandlerBC)
				.to(CYCLIC_XML_HANDLER, ITypeBasedHandlerExtendable.class).with(Byte.class);
		beanContextFactory.link(numberElementHandlerBC)
				.to(CYCLIC_XML_HANDLER, ITypeBasedHandlerExtendable.class).with(Short.class);
		beanContextFactory.link(numberElementHandlerBC)
				.to(CYCLIC_XML_HANDLER, ITypeBasedHandlerExtendable.class).with(Integer.class);
		beanContextFactory.link(numberElementHandlerBC)
				.to(CYCLIC_XML_HANDLER, ITypeBasedHandlerExtendable.class).with(Long.class);
		beanContextFactory.link(numberElementHandlerBC)
				.to(CYCLIC_XML_HANDLER, ITypeBasedHandlerExtendable.class).with(Float.class);
		beanContextFactory.link(numberElementHandlerBC)
				.to(CYCLIC_XML_HANDLER, ITypeBasedHandlerExtendable.class).with(Double.class);
		beanContextFactory.link(numberElementHandlerBC)
				.to(CYCLIC_XML_HANDLER, ITypeBasedHandlerExtendable.class).with(Boolean.class);
		beanContextFactory.link(numberElementHandlerBC)
				.to(CYCLIC_XML_HANDLER, ITypeBasedHandlerExtendable.class).with(Character.class);
		beanContextFactory.link(numberElementHandlerBC)
				.to(CYCLIC_XML_HANDLER, ITypeBasedHandlerExtendable.class).with(Byte.TYPE);
		beanContextFactory.link(numberElementHandlerBC)
				.to(CYCLIC_XML_HANDLER, ITypeBasedHandlerExtendable.class).with(Short.TYPE);
		beanContextFactory.link(numberElementHandlerBC)
				.to(CYCLIC_XML_HANDLER, ITypeBasedHandlerExtendable.class).with(Integer.TYPE);
		beanContextFactory.link(numberElementHandlerBC)
				.to(CYCLIC_XML_HANDLER, ITypeBasedHandlerExtendable.class).with(Long.TYPE);
		beanContextFactory.link(numberElementHandlerBC)
				.to(CYCLIC_XML_HANDLER, ITypeBasedHandlerExtendable.class).with(Float.TYPE);
		beanContextFactory.link(numberElementHandlerBC)
				.to(CYCLIC_XML_HANDLER, ITypeBasedHandlerExtendable.class).with(Double.TYPE);
		beanContextFactory.link(numberElementHandlerBC)
				.to(CYCLIC_XML_HANDLER, ITypeBasedHandlerExtendable.class).with(Boolean.TYPE);
		beanContextFactory.link(numberElementHandlerBC)
				.to(CYCLIC_XML_HANDLER, ITypeBasedHandlerExtendable.class).with(Character.TYPE);

		IBeanConfiguration dateTypeHandlerBC =
				beanContextFactory.registerBean(DateElementHandler.class).parent("abstractElementHandler");
		beanContextFactory.link(dateTypeHandlerBC)
				.to(CYCLIC_XML_HANDLER, INameBasedHandlerExtendable.class).with("d");

		IBeanConfiguration collectionElementHandlerBC = beanContextFactory
				.registerBean(CollectionElementHandler.class).parent("abstractElementHandler");
		beanContextFactory.link(collectionElementHandlerBC)
				.to(CYCLIC_XML_HANDLER, INameBasedHandlerExtendable.class).with("l");
		beanContextFactory.link(collectionElementHandlerBC)
				.to(CYCLIC_XML_HANDLER, INameBasedHandlerExtendable.class).with("set");

		beanContextFactory.registerBean("abstractElementHandler", AbstractHandler.class)
				.propertyRef("ClassElementHandler", classElementHandlerBC).template();

		IBeanConfiguration arrayElementHandlerBC =
				beanContextFactory.registerBean(ArrayNameHandler.class).parent("abstractElementHandler");
		beanContextFactory.link(arrayElementHandlerBC)
				.to(CYCLIC_XML_HANDLER, INameBasedHandlerExtendable.class).with("a");

		IBeanConfiguration enumElementHandlerBC =
				beanContextFactory.registerBean(EnumNameHandler.class).parent("abstractElementHandler");
		beanContextFactory.link(enumElementHandlerBC)
				.to(CYCLIC_XML_HANDLER, INameBasedHandlerExtendable.class).with("e");

		IBeanConfiguration timeSpanElementHandlerBC = beanContextFactory
				.registerBean(TimeSpanElementHandler.class).parent("abstractElementHandler");
		beanContextFactory.link(timeSpanElementHandlerBC)
				.to(CYCLIC_XML_HANDLER, INameBasedHandlerExtendable.class).with("t");

		beanContextFactory.registerBean("xmlTransferScanner", XmlTransferScanner.class);

		beanContextFactory.registerBean("xmlDictionary", CyclicXmlDictionary.class)
				.autowireable(ICyclicXmlDictionary.class);

		ExtendableBean.registerExtendableBean(beanContextFactory, "objectFutureHandlerExtendable",
				IObjectFutureHandlerRegistry.class, IObjectFutureHandlerExtendable.class,
				IObjectFutureHandlerRegistry.class.getClassLoader());

		beanContextFactory.registerBean("objRefFutureHandler", ObjRefFutureHandler.class);
		beanContextFactory.link("objRefFutureHandler").to(IObjectFutureHandlerExtendable.class)
				.with(ObjRefFuture.class);

		beanContextFactory.registerBean("prefetchFutureHandler", PrefetchFutureHandler.class);
		beanContextFactory.link("prefetchFutureHandler").to(IObjectFutureHandlerExtendable.class)
				.with(PrefetchFuture.class);

		ExtendableBean
				.registerExtendableBean(beanContextFactory, "xmlPostProcessorRegistry",
						IXmlPostProcessorRegistry.class, IXmlPostProcessorExtendable.class,
						IXmlPostProcessorRegistry.class.getClassLoader())
				.propertyValue("ArgumentTypes", new Class<?>[] {String.class});

		beanContextFactory.registerBean("mergeXmlPostProcessor", MergeXmlPostProcessor.class);
		beanContextFactory.link("mergeXmlPostProcessor").to(IXmlPostProcessorExtendable.class)
				.with("merge");

		IBeanConfiguration byteBufferCC =
				beanContextFactory.registerBean(ByteBuffer65536CollectableController.class);
		beanContextFactory.link(byteBufferCC).to(ICollectableControllerExtendable.class)
				.with(ByteBuffer.class);

		beanContextFactory.registerBean(CUDResultPrinter.class).autowireable(ICUDResultPrinter.class);

	}
}
