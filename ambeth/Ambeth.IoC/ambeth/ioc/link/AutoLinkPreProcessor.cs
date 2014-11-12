using De.Osthus.Ambeth.Config;
using De.Osthus.Ambeth.Ioc;
using De.Osthus.Ambeth.Ioc.Config;
using De.Osthus.Ambeth.Ioc.Extendable;
using De.Osthus.Ambeth.Ioc.Factory;
using De.Osthus.Ambeth.Ioc.Link;
using De.Osthus.Ambeth.Log;
using De.Osthus.Ambeth.Typeinfo;
using De.Osthus.Ambeth.Util;
using System;
using System.Collections.Generic;
using System.Reflection;

public class AutoLinkPreProcessor : IInitializingBean, IBeanPreProcessor
{
    [LogInstance]
    public ILogger Log { private get; set; }

    protected IExtendableRegistry extendableRegistry;

    protected Type extensionType;

    protected String extendableName;

    protected Type extendableType;

    public void AfterPropertiesSet()
    {
        ParamChecker.AssertNotNull(extendableType, "extendableType");
        if (extensionType == null)
        {
            ParamChecker.AssertNotNull(extendableRegistry, "extendableRegistry");
            MethodInfo[] addRemoveMethods = extendableRegistry.GetAddRemoveMethods(extendableType);
            extensionType = addRemoveMethods[0].GetParameters()[0].ParameterType;
        }
    }

    public void SetExtendableName(String extendableName)
    {
        this.extendableName = extendableName;
    }

    public void SetExtendableRegistry(IExtendableRegistry extendableRegistry)
    {
        this.extendableRegistry = extendableRegistry;
    }

    public void SetExtendableType(Type extendableType)
    {
        this.extendableType = extendableType;
    }

    public void SetExtensionType(Type extensionType)
    {
        this.extensionType = extensionType;
    }

    public void PreProcessProperties(IBeanContextFactory beanContextFactory, IProperties props, String beanName, Object service, Type beanType, IList<IPropertyConfiguration> propertyConfigs, IPropertyInfo[] properties)
    {
        if (extensionType.IsAssignableFrom(service.GetType()))
        {
            if (Log.DebugEnabled)
            {
                if (extendableName == null)
                {
                    Log.Debug("Registering bean '" + beanName + "' to " + extendableType.Name + " because it implements "
                            + extensionType.Name);
                }
                else
                {
                    Log.Debug("Registering bean '" + beanName + "' to " + extendableType.Name + " ('" + extendableName + "') because it implements "
                            + extensionType.Name);
                }
            }
            ILinkRegistryNeededConfiguration<Object> link = beanContextFactory.Link(service);
            if (extendableName == null)
            {
                link.To(extendableType);
            }
            else
            {
                link.To(extendableName, extendableType);
            }
        }
    }
}
