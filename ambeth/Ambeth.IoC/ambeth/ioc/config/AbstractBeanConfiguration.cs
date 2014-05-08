using System;
using System.Collections.Generic;
using De.Osthus.Ambeth.Collections;
using De.Osthus.Ambeth.Config;
using De.Osthus.Ambeth.Util;

namespace De.Osthus.Ambeth.Ioc.Config
{
    public abstract class AbstractBeanConfiguration : IBeanConfiguration
    {
        public static readonly CHashSet<String> ignoreClassNames = new CHashSet<String>(0.5f);

        static AbstractBeanConfiguration()
        {
            //ignoreClassNames.Add(Thread.class.getName());
            ignoreClassNames.Add(typeof(AbstractBeanConfiguration).FullName);
            ignoreClassNames.Add(typeof(BeanConfiguration).FullName);
            ignoreClassNames.Add(typeof(BeanInstanceConfiguration).FullName);
        }

        protected readonly String beanName;

        protected readonly IProperties props;

        protected String parentBeanName;

        protected IList<Type> autowireableTypes;

        protected IList<IPropertyConfiguration> propertyConfigurations;

        protected IList<String> ignoredProperties;

        protected bool overridesExistingField;

        protected PrecedenceType precedenceValue = PrecedenceType.DEFAULT;

        protected String declarationStackTrace;

        public AbstractBeanConfiguration(String beanName, IProperties props)
        {
            this.beanName = beanName;
            this.props = props;
            declarationStackTrace = AbstractPropertyConfiguration.GetCurrentStackTraceCompact(ignoreClassNames, props);
        }

        public String GetDeclarationStackTrace()
        {
            return declarationStackTrace;
        }

        public PrecedenceType GetPrecedence()
        {
            return precedenceValue;
        }

        public IBeanConfiguration Precedence(PrecedenceType precedenceType)
        {
            precedenceValue = precedenceType;
            return this;
        }

        public IBeanConfiguration Autowireable<T>()
        {
            return Autowireable(typeof(T));
        }

        public IBeanConfiguration Autowireable(Type typeToPublish)
        {
            ParamChecker.AssertParamNotNull(typeToPublish, "typeToPublish");
            if (autowireableTypes == null)
            {
                autowireableTypes = new List<Type>();
            }
            autowireableTypes.Add(typeToPublish);
            return this;
        }

        public IBeanConfiguration Autowireable(params Type[] typesToPublish)
        {
            ParamChecker.AssertParamNotNull(typesToPublish, "typesToPublish");
            foreach (Type typeToPublish in typesToPublish)
            {
                Autowireable(typeToPublish);
            }
            return this;
        }

        public IBeanConfiguration OverridesExisting()
	    {
		    this.overridesExistingField = true;
		    return this;
	    }

	    public bool IsOverridesExisting()
	    {
            return overridesExistingField;
	    }

        public IBeanConfiguration Parent(String parentBeanTemplateName)
        {
            if (this.parentBeanName != null)
            {
                throw new System.Exception("There is already a parent bean defined");
            }
            this.parentBeanName = parentBeanTemplateName;
            return this;
        }

        public IBeanConfiguration PropertyRef(String propertyName, String beanName)
        {
            ParamChecker.AssertParamNotNull(propertyName, "propertyName");
            ParamChecker.AssertParamNotNull(beanName, "beanName");
            if (propertyConfigurations == null)
            {
                propertyConfigurations = new List<IPropertyConfiguration>();
            }
            propertyConfigurations.Add(new PropertyRefConfiguration(this, propertyName, beanName, props));
            return this;
        }

        public IBeanConfiguration PropertyRefs(String beanName)
        {
            ParamChecker.AssertParamNotNull(beanName, "beanName");
            if (propertyConfigurations == null)
            {
                propertyConfigurations = new List<IPropertyConfiguration>();
            }
            propertyConfigurations.Add(new PropertyRefConfiguration(this, beanName, props));
            return this;
        }

        public IBeanConfiguration PropertyRefs(params String[] beanNames)
        {
            if (beanNames == null || beanNames.Length == 0)
            {
                throw new System.Exception("Array of beanNames must have a length of at least 1");
            }
            for (int a = 0, size = beanNames.Length; a < size; a++)
            {
                PropertyRefs(beanNames[a]);
            }
            return this;
        }

        public IBeanConfiguration PropertyRef(String propertyName, IBeanConfiguration bean)
        {
            ParamChecker.AssertParamNotNull(propertyName, "propertyName");
            ParamChecker.AssertParamNotNull(bean, "bean");
            if (propertyConfigurations == null)
            {
                propertyConfigurations = new List<IPropertyConfiguration>();
            }
            propertyConfigurations.Add(new PropertyEmbeddedRefConfiguration(this, propertyName, bean, props));
            return this;
        }

        public IBeanConfiguration PropertyRef(IBeanConfiguration bean)
        {
            ParamChecker.AssertParamNotNull(bean, "bean");
            if (propertyConfigurations == null)
            {
                propertyConfigurations = new List<IPropertyConfiguration>();
            }
            propertyConfigurations.Add(new PropertyEmbeddedRefConfiguration(this, bean, props));
            return this;
        }

        public IBeanConfiguration PropertyValue(String propertyName, Object value)
        {
            ParamChecker.AssertParamNotNull(propertyName, "propertyName");
            if (propertyConfigurations == null)
            {
                propertyConfigurations = new List<IPropertyConfiguration>();
            }
            propertyConfigurations.Add(new PropertyValueConfiguration(this, propertyName, value, props));
            return this;
        }

        public IBeanConfiguration IgnoreProperties(String propertyName)
        {
            ParamChecker.AssertParamNotNull(propertyName, "propertyName");
            if (ignoredProperties == null)
            {
                ignoredProperties = new List<String>();
            }
            ignoredProperties.Add(propertyName);
            return this;
        }

        public IBeanConfiguration IgnoreProperties(params String[] propertyNames)
        {
            if (propertyNames == null || propertyNames.Length == 0)
            {
                throw new System.Exception("Array of propertyNames must have a length of at least 1");
            }
            for (int a = 0, size = propertyNames.Length; a < size; a++)
            {
                IgnoreProperties(propertyNames[a]);
            }
            return this;
        }

        public String GetName()
        {
            return beanName;
        }

        public String GetParentName()
        {
            return parentBeanName;
        }

        public IList<Type> GetAutowireableTypes()
        {
            return autowireableTypes;
        }

        public IList<IPropertyConfiguration> GetPropertyConfigurations()
        {
            return propertyConfigurations;
        }

        public IList<String> GetIgnoredPropertyNames()
        {
            return ignoredProperties;
        }

        public virtual Object GetInstance()
        {
            return GetInstance(GetBeanType());
        }

        public virtual bool IsAbstract()
        {
            return false;
        }

        public virtual bool IsWithLifecycle()
        {
            return true;
        }

        public virtual IBeanConfiguration Template()
        {
            throw new NotSupportedException();
        }

        abstract public Type GetBeanType();

        abstract public Object GetInstance(Type type);
    }
}