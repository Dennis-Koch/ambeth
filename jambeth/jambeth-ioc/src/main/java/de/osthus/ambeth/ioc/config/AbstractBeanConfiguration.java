package de.osthus.ambeth.ioc.config;

import java.util.List;

import de.osthus.ambeth.collections.ArrayList;
import de.osthus.ambeth.collections.HashSet;
import de.osthus.ambeth.config.IProperties;
import de.osthus.ambeth.ioc.ServiceContext;
import de.osthus.ambeth.ioc.factory.BeanContextFactory;
import de.osthus.ambeth.ioc.link.LinkConfiguration;
import de.osthus.ambeth.ioc.link.LinkController;
import de.osthus.ambeth.util.ParamChecker;

public abstract class AbstractBeanConfiguration implements IBeanConfiguration
{
	protected static final HashSet<String> ignoreClassNames = new HashSet<String>(0.5f);

	static
	{
		ignoreClassNames.add(Thread.class.getName());
		ignoreClassNames.add(AbstractBeanConfiguration.class.getName());
		ignoreClassNames.add(AbstractPropertyConfiguration.class.getName());
		ignoreClassNames.add(BeanConfiguration.class.getName());
		ignoreClassNames.add(BeanContextFactory.class.getName());
		ignoreClassNames.add(BeanRuntime.class.getName());
		ignoreClassNames.add(BeanInstanceConfiguration.class.getName());
		ignoreClassNames.add(LinkConfiguration.class.getName());
		ignoreClassNames.add(LinkController.class.getName());
		ignoreClassNames.add(ServiceContext.class.getName());
	}

	protected final String beanName;

	protected final IProperties props;

	protected String parentBeanName;

	protected List<Class<?>> autowireableTypes;

	protected List<IPropertyConfiguration> propertyConfigurations;

	protected List<String> ignoredProperties;

	protected boolean overridesExistingField;

	protected PrecedenceType precedenceValue = PrecedenceType.DEFAULT;

	protected StackTraceElement[] declarationStackTrace;

	public AbstractBeanConfiguration(String beanName, IProperties props)
	{
		this.beanName = beanName;
		this.props = props;
		declarationStackTrace = AbstractPropertyConfiguration.getCurrentStackTraceCompact(ignoreClassNames, props);
	}

	@Override
	public StackTraceElement[] getDeclarationStackTrace()
	{
		return declarationStackTrace;
	}

	@Override
	public PrecedenceType getPrecedence()
	{
		return precedenceValue;
	}

	@Override
	public IBeanConfiguration precedence(PrecedenceType precedenceType)
	{
		precedenceValue = precedenceType;
		return this;
	}

	@Override
	public IBeanConfiguration autowireable(Class<?> typeToPublish)
	{
		ParamChecker.assertParamNotNull(typeToPublish, "typeToPublish");
		if (autowireableTypes == null)
		{
			autowireableTypes = new ArrayList<Class<?>>();
		}
		autowireableTypes.add(typeToPublish);
		return this;
	}

	@Override
	public IBeanConfiguration autowireable(Class<?>... typesToPublish)
	{
		ParamChecker.assertParamNotNull(typesToPublish, "typesToPublish");
		for (Class<?> typeToPublish : typesToPublish)
		{
			autowireable(typeToPublish);
		}
		return this;
	}

	@Override
	public IBeanConfiguration overridesExisting()
	{
		overridesExistingField = true;
		return this;
	}

	@Override
	public boolean isOverridesExisting()
	{
		return overridesExistingField;
	}

	@Override
	public IBeanConfiguration parent(String parentBeanTemplateName)
	{
		if (parentBeanName != null)
		{
			throw new UnsupportedOperationException("There is already a parent bean defined");
		}
		parentBeanName = parentBeanTemplateName;
		return this;
	}

	@Override
	public IBeanConfiguration propertyRef(String propertyName, String beanName)
	{
		ParamChecker.assertParamNotNull(propertyName, "propertyName");
		ParamChecker.assertParamNotNull(beanName, "beanName");
		if (propertyConfigurations == null)
		{
			propertyConfigurations = new ArrayList<IPropertyConfiguration>();
		}
		propertyConfigurations.add(new PropertyRefConfiguration(this, propertyName, beanName, props));
		return this;
	}

	@Override
	public IBeanConfiguration propertyRefs(String beanName)
	{
		ParamChecker.assertParamNotNull(beanName, "beanName");
		if (propertyConfigurations == null)
		{
			propertyConfigurations = new ArrayList<IPropertyConfiguration>();
		}
		propertyConfigurations.add(new PropertyRefConfiguration(this, beanName, props));
		return this;
	}

	@Override
	public IBeanConfiguration propertyRefs(String... beanNames)
	{
		if (beanNames == null || beanNames.length == 0)
		{
			throw new IllegalArgumentException("Array of beanNames must have a length of at least 1");
		}
		for (int a = 0, size = beanNames.length; a < size; a++)
		{
			propertyRefs(beanNames[a]);
		}
		return this;
	}

	@Override
	public IBeanConfiguration propertyRef(String propertyName, IBeanConfiguration bean)
	{
		ParamChecker.assertParamNotNull(propertyName, "propertyName");
		ParamChecker.assertParamNotNull(bean, "bean");
		if (propertyConfigurations == null)
		{
			propertyConfigurations = new ArrayList<IPropertyConfiguration>();
		}
		propertyConfigurations.add(new PropertyEmbeddedRefConfiguration(this, propertyName, bean, props));
		return this;
	}

	@Override
	public IBeanConfiguration propertyRef(IBeanConfiguration bean)
	{
		ParamChecker.assertParamNotNull(bean, "bean");
		if (propertyConfigurations == null)
		{
			propertyConfigurations = new ArrayList<IPropertyConfiguration>();
		}
		propertyConfigurations.add(new PropertyEmbeddedRefConfiguration(this, bean, props));
		return this;
	}

	@Override
	public IBeanConfiguration propertyRefs(IBeanConfiguration... beans)
	{
		if (beans == null || beans.length == 0)
		{
			throw new IllegalArgumentException("Array of beans must have a length of at least 1");
		}
		for (int a = 0, size = beans.length; a < size; a++)
		{
			propertyRef(beans[a]);
		}
		return this;
	}

	@Override
	public IBeanConfiguration propertyValue(String propertyName, Object value)
	{
		ParamChecker.assertParamNotNull(propertyName, "propertyName");
		if (propertyConfigurations == null)
		{
			propertyConfigurations = new ArrayList<IPropertyConfiguration>();
		}
		propertyConfigurations.add(new PropertyValueConfiguration(this, propertyName, value, props));
		return this;
	}

	@Override
	public IBeanConfiguration ignoreProperties(String propertyName)
	{
		ParamChecker.assertParamNotNull(propertyName, "propertyName");
		if (ignoredProperties == null)
		{
			ignoredProperties = new ArrayList<String>();
		}
		ignoredProperties.add(propertyName);
		return this;
	}

	@Override
	public IBeanConfiguration ignoreProperties(String... propertyNames)
	{
		if (propertyNames == null || propertyNames.length == 0)
		{
			throw new IllegalArgumentException("Array of propertyNames must have a length of at least 1");
		}
		for (int a = 0, size = propertyNames.length; a < size; a++)
		{
			ignoreProperties(propertyNames[a]);
		}
		return this;
	}

	@Override
	public String getName()
	{
		return beanName;
	}

	@Override
	public String getParentName()
	{
		return parentBeanName;
	}

	@Override
	public boolean isWithLifecycle()
	{
		return true;
	}

	@Override
	public List<Class<?>> getAutowireableTypes()
	{
		return autowireableTypes;
	}

	@Override
	public List<IPropertyConfiguration> getPropertyConfigurations()
	{
		return propertyConfigurations;
	}

	@Override
	public List<String> getIgnoredPropertyNames()
	{
		return ignoredProperties;
	}

	@Override
	public Object getInstance()
	{
		return getInstance(getBeanType());
	}

	@Override
	public boolean isAbstract()
	{
		return false;
	}

	@Override
	public IBeanConfiguration template()
	{
		throw new UnsupportedOperationException();
	}

	@Override
	public abstract Class<?> getBeanType();

	@Override
	public String toString()
	{
		String name = getName();
		return name != null ? name : super.toString();
	}
}
