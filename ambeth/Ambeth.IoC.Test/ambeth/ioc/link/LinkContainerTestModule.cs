using System;
using De.Osthus.Ambeth.Ioc;
using De.Osthus.Ambeth.Config;
using De.Osthus.Ambeth.Ioc.Factory;
using De.Osthus.Ambeth.Ioc.Config;
using System.ComponentModel;

namespace De.Osthus.Ambeth.Ioc.Link
{
    public class LinkContainerTestModule : IInitializingModule
    {
        [Property(LinkContainerTest.ExtendableTypeProp, Mandatory = false)]
        public Type ExtendableType { protected get; set; }

        [Property(LinkContainerTest.ListenerProp)]
        public ListenerVariant ListenerVariant { protected get; set; }

        [Property(LinkContainerTest.RegistryProp)]
        public RegistryVariant RegistryVariant { protected get; set; }

        [Property(LinkContainerTest.ListenerNameProp, Mandatory = false)]
        public String ListenerName { protected get; set; }

        [Property(LinkContainerTest.OptionalProp, DefaultValue = "false")]
        public bool Optional { protected get; set; }

        public void AfterPropertiesSet(IBeanContextFactory beanContextFactory)
        {
            IBeanConfiguration registryC = beanContextFactory.RegisterBean<TestRegistry>(LinkContainerTest.REGISTRY_NAME).Autowireable(typeof(ITestListenerExtendable),
                    typeof(ITestRegistry), typeof(INotifyPropertyChanged));
            IBeanConfiguration listenerC = beanContextFactory.RegisterBean<TestListener>(LinkContainerTest.LISTENER_NAME);

            if (ListenerName == null)
            {
                ListenerName = LinkContainerTest.LISTENER_NAME;
            }
            String registryPropertyName = LinkContainerTest.REGISTRY_PROPERTY_NAME;
            ILinkRegistryNeededConfiguration link1;
            switch (ListenerVariant)
            {
                case ListenerVariant.BY_NAME:
                    link1 = beanContextFactory.Link(ListenerName);
                    break;
                case ListenerVariant.BY_NAME_DELEGATE:
                    if (ExtendableType == null)
                    {
                        ExtendableType = typeof(INotifyPropertyChanged);
                    }
                    beanContextFactory.RegisterExternalBean(LinkContainerTest.LISTENER_DELEGATE_NAME, new PropertyChangedEventHandler(((TestListener)listenerC.GetInstance()).HandlePropertyChangedTest));
                    link1 = beanContextFactory.Link(LinkContainerTest.LISTENER_DELEGATE_NAME);
                    registryPropertyName = LinkContainerTest.REGISTRY_EVENT_PROPERTY_NAME;
                    break;
                case ListenerVariant.BY_NAME_AND_METHOD:
                    link1 = beanContextFactory.Link(ListenerName, "HandlePropertyChangedTest");
                    break;
                case ListenerVariant.BY_CONF:
                    link1 = beanContextFactory.Link(listenerC);
                    break;
                case ListenerVariant.BY_CONF_DELEGATE:
                    if (ExtendableType == null)
                    {
                        ExtendableType = typeof(INotifyPropertyChanged);
                    }
                    IBeanConfiguration listenerDelegateC = beanContextFactory.RegisterExternalBean(LinkContainerTest.LISTENER_DELEGATE_NAME, new PropertyChangedEventHandler(((TestListener)listenerC.GetInstance()).HandlePropertyChangedTest));
                    link1 = beanContextFactory.Link(listenerDelegateC);
                    registryPropertyName = LinkContainerTest.REGISTRY_EVENT_PROPERTY_NAME;
                    break;
                case ListenerVariant.BY_INSTANCE:
                    link1 = beanContextFactory.Link(listenerC.GetInstance());
                    break;
                case ListenerVariant.BY_INSTANCE_DELEGATE:
                    if (ExtendableType == null)
                    {
                        ExtendableType = typeof(INotifyPropertyChanged);
                    }
                    link1 = beanContextFactory.Link(new PropertyChangedEventHandler(((TestListener)listenerC.GetInstance()).HandlePropertyChangedTest));
                    registryPropertyName = LinkContainerTest.REGISTRY_EVENT_PROPERTY_NAME;
                    break;
                default:
                    throw new System.Exception("Unsupported enum: " + ListenerVariant);
            }

            if (ExtendableType == null)
            {
                ExtendableType = typeof(ITestListenerExtendable);
            }
            ILinkConfigWithOptional link2;
            switch (RegistryVariant)
            {
                case RegistryVariant.BY_EXTENDABLE:
                    link2 = link1.To(ExtendableType);
                    break;
                case RegistryVariant.BY_NAME_AND_EXTENDABLE:
                    link2 = link1.To(LinkContainerTest.REGISTRY_NAME, ExtendableType);
                    break;
                case RegistryVariant.BY_NAME_AND_EVENT:
                    link2 = link1.To(LinkContainerTest.REGISTRY_NAME, new EventDelegate<Object>(registryPropertyName));
                    break;
                case RegistryVariant.BY_NAME_AND_PROPERTY:
                    link2 = link1.To(LinkContainerTest.REGISTRY_NAME, registryPropertyName);
                    break;
                case RegistryVariant.BY_INSTANCE_AND_EXTENDABLE:
                    link2 = link1.To(registryC.GetInstance(), ExtendableType);
                    break;
                case RegistryVariant.BY_INSTANCE_AND_EVENT:
                    link2 = link1.To(registryC.GetInstance(), new EventDelegate<Object>(registryPropertyName));
                    break;
                case RegistryVariant.BY_INSTANCE_AND_PROPERTY:
                    link2 = link1.To(registryC.GetInstance(), registryPropertyName);
                    break;
                default:
                    throw new System.Exception("Unsupported enum: " + RegistryVariant);
            }
            if (Optional)
            {
                link2.Optional();
                link2 = null;
            }
        }
    }

}
