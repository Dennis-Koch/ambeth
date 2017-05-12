using System;
using System.Collections.Generic;
using System.Collections.ObjectModel;
using System.Threading;
using De.Osthus.Ambeth.Util;
using De.Osthus.Ambeth.Ioc;
using De.Osthus.Ambeth.Service;
using De.Osthus.Ambeth.Cache;
using De.Osthus.Ambeth.Log;
using De.Osthus.Ambeth.Datachange;
using De.Osthus.Ambeth.Event;
using De.Osthus.Ambeth.Datachange.Model;
using De.Osthus.Minerva.Util;
using De.Osthus.Minerva.Core;
using De.Osthus.Ambeth.Ioc.Factory;
using De.Osthus.Ambeth.Ioc.Config;

namespace De.Osthus.Minerva.Bind
{
    public class DataChangeControllerPostProcessor : IBeanPostProcessor, IInitializingBean
    {
        public static readonly Type dataChangeControllerType = typeof(ViewModelDataChangeController<>);

        public virtual void AfterPropertiesSet()
        {
        }

        public virtual Object PostProcessBean(IBeanContextFactory beanContextFactory, IServiceContext beanContext, IBeanConfiguration beanConfiguration, Type beanType, Object targetBean, ISet<Type> requestedTypes)
        {
            if (!beanType.IsGenericType)
            {
                return targetBean;
            }
            // ViewModelDataChangeController has exactly 1 generic argument
            Type genericTypeArgument = beanType.GetGenericArguments()[0];
            // Instantiate TYPE to generic argument of given bean type
            if (!dataChangeControllerType.MakeGenericType(genericTypeArgument).IsAssignableFrom(beanType))
            {
                return targetBean;
            }
            Type[] arguments = beanType.GetGenericArguments();
            Object eventListener = TypeFilteredDataChangeListener.CreateEventListener((IDataChangeListener)targetBean, arguments[0]);
            String eventListenerBeanName = beanConfiguration.GetName() + ".eventListener";
            beanContextFactory.RegisterExternalBean(eventListenerBeanName, eventListener);

            if (beanContext.IsRunning)
            {
                beanContext.Link(eventListenerBeanName).To<IEventListenerExtendable>().With(typeof(IDataChange));
            }
            else
            {
                beanContextFactory.Link(eventListenerBeanName).To<IEventListenerExtendable>().With(typeof(IDataChange));
            }
            return targetBean;
        }
    }
}
