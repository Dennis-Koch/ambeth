package de.osthus.ambeth.xml.postprocess;

import de.osthus.ambeth.ioc.IInitializingModule;
import de.osthus.ambeth.ioc.factory.IBeanContextFactory;
import de.osthus.ambeth.log.ILogger;
import de.osthus.ambeth.log.LogInstance;

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
