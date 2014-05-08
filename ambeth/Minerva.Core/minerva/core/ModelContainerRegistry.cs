using System;
using System.Collections;
using System.Collections.Generic;
using System.ComponentModel;
using De.Osthus.Ambeth.Ioc;
using De.Osthus.Ambeth.Util;
using De.Osthus.Ambeth.Ioc.Extendable;

namespace De.Osthus.Minerva.Core
{
    public class ModelContainerRegistry : IInitializingBean, IDisposableBean, IModelContainerRegistry, INotPersistedDataContainerExtendable, INotifyModelRegistered
    {
        protected readonly PropertyChangedEventHandler notPersistedChangedDelegate;

        protected readonly IExtendableContainer<Object> npdcList = new DefaultExtendableContainer<Object>("ChangedDataContainer");
        
        public event PropertyChangedEventHandler NotPersistedChanged;

        public event PropertyChangedEventHandler ModelRegistered;

        public IServiceContext BeanContext { get; set; }
        
        public ModelContainerRegistry()
        {
            notPersistedChangedDelegate = new PropertyChangedEventHandler(OnNotPersistedChanged);
        }

        public virtual void AfterPropertiesSet()
        {
            ParamChecker.AssertNotNull(BeanContext, "BeanContext");
        }

        public virtual void OnNotPersistedChanged(Object sender, PropertyChangedEventArgs e)
        {
            if (NotPersistedChanged != null)
            {
                NotPersistedChanged.Invoke(this, e);
            }
        }

        //protected ControllerServiceDelegate GetControllerServiceDelegate(String mergeControllerName, String mergeMethod)
        //{
        //    Object mergeController = BeanContext.GetService(mergeControllerName);
        //    MethodInfo method = mergeController.GetType().GetMethod(mergeMethod);
        //    ParameterInfo[] paramInfos = method.GetParameters();

        //    return new ControllerServiceDelegate(delegate(Object[] args)
        //        {
        //            Object[] convertedArgs = new Object[args.Length];
        //            for (int a = args.Length; a-- > 0; )
        //            {
        //                Object arg = args[a];
        //                IList<Object> argAsList = ListUtil.AnyToList(arg);

        //                Type expectedType = paramInfos[a].ParameterType;

        //                if (typeof(IEnumerable).IsAssignableFrom(expectedType))
        //                {
        //                    ICollection targetCollection = ListUtil.CreateCollectionOfType(expectedType);

        //                    MethodInfo addMethod = targetCollection.GetType().GetMethod("Add");
        //                    Object[] parameters = new Object[1];

        //                    foreach (Object argItem in argAsList)
        //                    {
        //                        parameters[0] = argItem;
        //                        addMethod.Invoke(targetCollection, parameters);
        //                    }
        //                    convertedArgs[a] = targetCollection;
        //                }
        //                else if (expectedType.IsArray)
        //                {
        //                    Array array = Array.CreateInstance(expectedType.GetElementType(), argAsList.Count);
        //                    for (int b = argAsList.Count; b-- > 0;)
        //                    {
        //                        array.SetValue(argAsList[b], b);
        //                    }
        //                    convertedArgs[a] = array;
        //                }
        //            }
        //            return method.Invoke(mergeController, convertedArgs);
        //        });
        //}

        public void RegisterNotPersistedDataContainer(Object npdc)
        {
            npdcList.Register(npdc);
            ((INotPersistedDataContainer)npdc).NotPersistedChanged += notPersistedChangedDelegate;
            OnNotPersistedChanged(null, null);
            if (ModelRegistered != null)
            {
                ModelRegistered.Invoke(npdc, new PropertyChangedEventArgs("NewModel"));
            }
        }

        public void UnregisterNotPersistedDataContainer(Object npdc)
        {
            ((INotPersistedDataContainer)npdc).NotPersistedChanged -= notPersistedChangedDelegate;
            npdcList.Unregister(npdc);
            OnNotPersistedChanged(null, null);
            //TODO: Fire ModelRegistered Event for unregistering!
        }

        public void Destroy()
        {
            Object[] currList = npdcList.GetExtensions();
            for (int i = 0, length = currList.Length; i < length; i++)
            {
                ((INotPersistedDataContainer)currList[i]).NotPersistedChanged -= notPersistedChangedDelegate;
            }
        }

        public IList<Object> GetModelContainers()
        {
            IList<Object> result = new List<Object>();
            npdcList.GetExtensions(result);
            return result;
        }
    }
}
