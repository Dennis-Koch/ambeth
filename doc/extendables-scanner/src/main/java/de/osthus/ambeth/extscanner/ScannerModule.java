package de.osthus.ambeth.extscanner;

import de.osthus.ambeth.ioc.IInitializingModule;
import de.osthus.ambeth.ioc.config.PrecedenceType;
import de.osthus.ambeth.ioc.factory.IBeanContextFactory;
import de.osthus.ambeth.log.ILogger;
import de.osthus.ambeth.log.LogInstance;

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
