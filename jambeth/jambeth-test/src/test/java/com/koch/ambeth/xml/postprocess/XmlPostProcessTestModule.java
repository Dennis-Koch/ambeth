package com.koch.ambeth.xml.postprocess;

import com.koch.ambeth.ioc.IInitializingModule;
import com.koch.ambeth.ioc.factory.IBeanContextFactory;
import com.koch.ambeth.log.ILogger;
import com.koch.ambeth.log.LogInstance;
import com.koch.ambeth.xml.postprocess.IXmlPostProcessorExtendable;

public class XmlPostProcessTestModule implements IInitializingModule
{
	@SuppressWarnings("unused")
	@LogInstance
	private ILogger log;

	@Override
	public void afterPropertiesSet(IBeanContextFactory beanContextFactory) throws Throwable
	{
		beanContextFactory.registerBean("testXmlPostProcessor", TestXmlPostProcessor.class).autowireable(TestXmlPostProcessor.class);
		beanContextFactory.link("testXmlPostProcessor").to(IXmlPostProcessorExtendable.class).with("test1");
		beanContextFactory.link("testXmlPostProcessor").to(IXmlPostProcessorExtendable.class).with("test2");
		beanContextFactory.link("testXmlPostProcessor").to(IXmlPostProcessorExtendable.class).with("test3");
	}
}
