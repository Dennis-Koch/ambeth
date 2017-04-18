package com.koch.ambeth.extscanner;

import com.koch.ambeth.ioc.IInitializingModule;
import com.koch.ambeth.ioc.config.PrecedenceType;
import com.koch.ambeth.ioc.factory.IBeanContextFactory;
import com.koch.ambeth.log.ILogger;
import com.koch.ambeth.log.LogInstance;

public class ScannerModule implements IInitializingModule
{
	@SuppressWarnings("unused")
	@LogInstance
	private ILogger log;

	@Override
	public void afterPropertiesSet(IBeanContextFactory beanContextFactory) throws Throwable
	{
		beanContextFactory.registerAnonymousBean(XmlFilesScanner.class).autowireable(IXmlFilesScanner.class);
		beanContextFactory.registerAnonymousBean(AnnotationUpdater.class);
		beanContextFactory.registerAnonymousBean(ConfigurationUpdater.class);
		beanContextFactory.registerAnonymousBean(ExtendableUpdater.class);
		beanContextFactory.registerAnonymousBean(FeatureUpdater.class);
		beanContextFactory.registerAnonymousBean(ModuleUpdater.class).precedence(PrecedenceType.HIGH);

		beanContextFactory.registerAnonymousBean(Model.class).autowireable(IModel.class);
	}
}
