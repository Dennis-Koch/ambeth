package de.osthus.ambeth.ioc.link;

import java.util.List;
import java.util.Set;

import net.sf.cglib.reflect.FastMethod;
import de.osthus.ambeth.config.IProperties;
import de.osthus.ambeth.ioc.IBeanPreProcessor;
import de.osthus.ambeth.ioc.IInitializingBean;
import de.osthus.ambeth.ioc.IServiceContext;
import de.osthus.ambeth.ioc.config.IPropertyConfiguration;
import de.osthus.ambeth.ioc.extendable.IExtendableRegistry;
import de.osthus.ambeth.ioc.factory.IBeanContextFactory;
import de.osthus.ambeth.log.ILogger;
import de.osthus.ambeth.log.LogInstance;
import de.osthus.ambeth.typeinfo.IPropertyInfo;
import de.osthus.ambeth.util.ParamChecker;

public class AutoLinkPreProcessor implements IInitializingBean, IBeanPreProcessor
{
	@SuppressWarnings("unused")
	@LogInstance
	private ILogger log;

	protected IExtendableRegistry extendableRegistry;

	protected Class<?> extensionType;

	protected String extendableName;

	protected Class<?> extendableType;

	@Override
	public void afterPropertiesSet() throws Throwable
	{
		ParamChecker.assertNotNull(extendableType, "extendableType");
		if (extensionType == null)
		{
			ParamChecker.assertNotNull(extendableRegistry, "extendableRegistry");
			FastMethod[] addRemoveMethods = extendableRegistry.getAddRemoveMethods(extendableType);
			extensionType = addRemoveMethods[0].getParameterTypes()[0];
		}
	}

	public void setExtendableName(String extendableName)
	{
		this.extendableName = extendableName;
	}

	public void setExtendableRegistry(IExtendableRegistry extendableRegistry)
	{
		this.extendableRegistry = extendableRegistry;
	}

	public void setExtendableType(Class<?> extendableType)
	{
		this.extendableType = extendableType;
	}

	public void setExtensionType(Class<?> extensionType)
	{
		this.extensionType = extensionType;
	}

	@Override
	public void preProcessProperties(IBeanContextFactory beanContextFactory, IServiceContext beanContext, IProperties props, String beanName, Object service,
			Class<?> beanType, List<IPropertyConfiguration> propertyConfigs, Set<String> ignoredPropertyNames, IPropertyInfo[] properties)
	{
		if (extensionType.isAssignableFrom(service.getClass()))
		{
			if (log.isDebugEnabled())
			{
				if (extendableName == null)
				{
					log.debug("Registering bean '" + beanName + "' to " + extendableType.getSimpleName() + " because it implements "
							+ extensionType.getSimpleName());
				}
				else
				{
					log.debug("Registering bean '" + beanName + "' to " + extendableType.getSimpleName() + " ('" + extendableName + "') because it implements "
							+ extensionType.getSimpleName());
				}
			}
			ILinkRegistryNeededConfiguration<Object> link = beanContextFactory.link(service);
			if (extendableName == null)
			{
				link.to(extendableType);
			}
			else
			{
				link.to(extendableName, extendableType);
			}
		}
	}
}
