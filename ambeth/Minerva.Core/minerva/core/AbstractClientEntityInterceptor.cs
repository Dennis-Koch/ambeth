//using System;
//using De.Osthus.Ambeth.Cache;
//using De.Osthus.Ambeth.Ioc;
//using De.Osthus.Ambeth.Proxy;
//using De.Osthus.Ambeth.Util;
//#if SILVERLIGHT
//using Castle.Core.Interceptor;
//#else
//using Castle.DynamicProxy;
//#endif
//using System.Reflection;
//using System.ComponentModel;
//using System.Text.RegularExpressions;
//using De.Osthus.Ambeth.Typeinfo;
//using De.Osthus.Ambeth.Merge;
//using De.Osthus.Ambeth.Merge.Model;
//using System.Collections.Generic;
//using De.Osthus.Ambeth.Collections;
//using De.Osthus.Minerva.Core.Config;
//using De.Osthus.Ambeth.Config;
//using De.Osthus.Ambeth.Annotation;
//using De.Osthus.Ambeth.Model;
//using De.Osthus.Ambeth.Threading;
//using System.Collections;
//using System.Threading;
//using De.Osthus.Ambeth.Log;
//using System.Collections.Specialized;

//namespace De.Osthus.Minerva.Core
//{
//    public abstract class AbstractClientEntityInterceptor : IInterceptor, INotifyPropertyChanged, INotifyPropertyChangedSource
//    {
//        private static readonly ThreadLocal<bool> alreadyInInterceptorTL = new ThreadLocal<bool>();

//        public event PropertyChangedEventHandler PropertyChanged;

//        protected readonly ICacheModification cacheModification;

//        protected Object proxy;

//        protected readonly IDictionary<String, ClientEntityFactory.GetterItem> setterToGetterMethodDict;

//        protected readonly IDictionary<String, ClientEntityFactory.GetterItem> getterToSetterMethodDict;

//        public AbstractClientEntityInterceptor(IDictionary<String, ClientEntityFactory.GetterItem> setterToGetterMethodDict,
//            IDictionary<String, ClientEntityFactory.GetterItem> getterToSetterMethodDict, ICacheModification cacheModification)
//        {
//            this.setterToGetterMethodDict = setterToGetterMethodDict;
//            this.getterToSetterMethodDict = getterToSetterMethodDict;
//            this.cacheModification = cacheModification;
//        }
            
//        public void SetProxy(Object proxy)
//        {
//            if (this.proxy != null)
//            {
//                throw new Exception("Must never happen");
//            }
//            this.proxy = proxy;
//        }

//        protected virtual bool TryExtendedInterfaceHandling(IInvocation invocation, Type declaringType)
//        {
//            if (typeof(INotifyPropertyChanged).Equals(declaringType) || typeof(INotifyPropertyChangedSource).Equals(declaringType))
//            {
//                invocation.ReturnValue = invocation.Method.Invoke(this, invocation.Arguments);
//                return true;
//            }
//            return false;
//        }

//        protected abstract void SetToBeUpdated(bool value);

//        /// <summary>
//        /// Checks, if the oldValue differs from the new value. If so, the new value is set.
//        /// Several EventListeners are deregistered for the old value and registered for the new value.
//        /// </summary>
//        /// <param name="invocation">IInvocation of the Setter</param>
//        /// <param name="getterItem">The getter fitting to the setter in invocation</param>
//        protected void HandleSetter(IInvocation invocation, ClientEntityFactory.GetterItem getterItem)
//        {
//            MethodInfo getter = getterItem.getter;
//            Object oldValue = getter.Invoke(proxy, null);
//            Object newValue = invocation.Arguments[0];
//            bool valueChanged;
//            Type returnType = getter.ReturnType;
//            if (returnType.IsValueType)
//            {
//                valueChanged = !Object.Equals(oldValue, newValue);
//            }
//            else if (typeof(String).Equals(returnType))
//            {
//                bool oldIsEmpty = false, newIsEmpty = false;
//                if (oldValue == null || ((String)oldValue).Length == 0)
//                {
//                    oldIsEmpty = true;
//                }
//                if (newValue == null || ((String)newValue).Length == 0)
//                {
//                    newIsEmpty = true;
//                }
//                if (!oldIsEmpty && !newIsEmpty)
//                {
//                    valueChanged = !Object.Equals(oldValue, newValue);
//                }
//                else
//                {
//                    valueChanged = (oldIsEmpty != newIsEmpty);
//                }
//            }
//            else
//            {
//                valueChanged = !Object.ReferenceEquals(oldValue, newValue);
//            }
//            if (valueChanged)
//            {
//                //bool? isParentChildSetter = null;
//                //if (newValue is INotifyPropertyChanged
//                // || newValue is INotifyCollectionChanged
//                // || oldValue is INotifyPropertyChanged
//                // || oldValue is INotifyCollectionChanged)
//                //{
//                //    isParentChildSetter = AnnotationUtil.GetAnnotation<ParentChildAttribute>(getterItem.property, true) != null;
//                //}
//                //if (oldValue is INotifyCollectionChanged)
//                //{
//                //    ((INotifyCollectionChanged)oldValue).CollectionChanged -= OnCollectionChangedHandler;
//                //    if (isParentChildSetter == true)
//                //    {
//                //        ((INotifyCollectionChanged)oldValue).CollectionChanged -= OnChildCollectionChangedHandler;
//                //    }
//                //}
//                //if (oldValue is INotifyPropertyChanged && isParentChildSetter == true)
//                //{
//                //    ((INotifyPropertyChanged)oldValue).PropertyChanged -= OnChildPropertyChangedHandler;
//                //}
//                //invocation.Proceed();
//                //if (newValue is INotifyPropertyChanged && isParentChildSetter == true)
//                //{
//                //    ((INotifyPropertyChanged)newValue).PropertyChanged += OnChildPropertyChangedHandler;
//                //}
//                //if (newValue is INotifyCollectionChanged)
//                //{
//                //    ((INotifyCollectionChanged)newValue).CollectionChanged += OnCollectionChangedHandler;
//                //    if (isParentChildSetter == true)
//                //    {
//                //        ((INotifyCollectionChanged)newValue).CollectionChanged += OnChildCollectionChangedHandler;
//                //    }
//                //}
//                //String[] propertyNames = getterItem.propertyNames;
//                //for (int a = 0, size = propertyNames.Length; a < size; a++)
//                //{
//                //    OnPropertyChanged(propertyNames[a]);
//                //}
//                //if (!CacheModification.IsActive)
//                //{
//                //    SetToBeUpdated(true);
//                //}
//                if (oldValue is INotifyCollectionChanged)
//                {
//                    // CacheModification is necessary to suppress eager load of value holders only because of this OnCollectionChanged-delegate
//                    bool oldCacheModification = cacheModification.Active;
//                    cacheModification.Active = true;
//                    try
//                    {
//                        ((INotifyCollectionChanged)oldValue).CollectionChanged -= OnCollectionChanged;
//                    }
//                    finally
//                    {
//                        cacheModification.Active = oldCacheModification;
//                    }
//                }
//                bool? isParentChildSetter = null;
//                if (oldValue is INotifyPropertyChanged)
//                {
//                    isParentChildSetter = AnnotationUtil.GetAnnotation<ParentChildAttribute>(getterItem.property, true) != null;
//                    if (isParentChildSetter == true)
//                    {
//                        // CacheModification is necessary to suppress eager load of value holders only because of this OnCollectionChanged-delegate
//                        bool oldCacheModification = cacheModification.Active;
//                        cacheModification.Active = true;
//                        try
//                        {
//                            ((INotifyPropertyChanged)oldValue).PropertyChanged -= OnChildPropertyChanged;
//                        }
//                        finally
//                        {
//                            cacheModification.Active = oldCacheModification;
//                        }
//                    }
//                }
//                invocation.Proceed();
//                if (newValue is INotifyPropertyChanged)
//                {
//                    if (isParentChildSetter == null)
//                    {
//                        isParentChildSetter = AnnotationUtil.GetAnnotation<ParentChildAttribute>(getterItem.property, true) != null;
//                    }
//                    if (isParentChildSetter == true)
//                    {
//                        // CacheModification is necessary to suppress eager load of value holders only because of this OnCollectionChanged-delegate
//                        bool oldCacheModification = cacheModification.Active;
//                        cacheModification.Active = true;
//                        try
//                        {
//                            ((INotifyPropertyChanged)newValue).PropertyChanged += OnChildPropertyChanged;
//                        }
//                        finally
//                        {
//                            cacheModification.Active = oldCacheModification;
//                        }
//                    }
//                }
//                if (newValue is INotifyCollectionChanged)
//                {
//                    // CacheModification is necessary to suppress eager load of value holders only because of this OnCollectionChanged-delegate
//                    bool oldCacheModification = cacheModification.Active;
//                    cacheModification.Active = true;
//                    try
//                    {
//                        ((INotifyCollectionChanged)newValue).CollectionChanged += OnCollectionChanged;
//                    }
//                    finally
//                    {
//                        cacheModification.Active = oldCacheModification;
//                    }
//                }
//                String[] propertyNames = getterItem.propertyNames;
//                for (int a = 0, size = propertyNames.Length; a < size; a++)
//                {
//                    OnPropertyChanged(propertyNames[a]);
//                }
//                if (!cacheModification.Active)
//                {
//                    SetToBeUpdated(true);
//                }
//            }
//        }

//        /// <summary>
//        /// Ensures, that an empty observable collection is returned instead of null.
//        /// This includes calling the setter, so the entity stores the empty collection.
//        /// </summary>
//        /// <param name="invocation">IInvocation of a getter</param>
//        /// <param name="setterItem">Setter fitting to the getter in invocation</param>
//        protected void HandleGetter(IInvocation invocation, ClientEntityFactory.GetterItem setterItem)
//        {
//            // Execute getter and analyse result
//            invocation.Proceed();
//            Object value = invocation.ReturnValue;
//            if (value != null || cacheModification.Active)
//            {
//                return;
//            }
//            Type returnType = invocation.Method.ReturnType;
//            if (!typeof(IEnumerable).IsAssignableFrom(returnType) || typeof(String).Equals(returnType))
//            {
//                return;
//            }
//            value = ListUtil.CreateObservableCollectionOfType(returnType);
//            setterItem.getter.Invoke(invocation.InvocationTarget, new Object[] { value });
//            invocation.ReturnValue = value;
//        }

//        public void Intercept(IInvocation invocation)
//        {
//            MethodInfo method = invocation.Method;
//            Type declaringType = method.DeclaringType;

//            if (TryExtendedInterfaceHandling(invocation, declaringType))
//            {
//                return;
//            }
//            bool alreadyState = alreadyInInterceptorTL.Value;
//            if (alreadyState || proxy == null)
//            {
//                invocation.Proceed();
//                return;
//            }
//            alreadyInInterceptorTL.Value = true;
//            try
//            {                    
//                //Note: We call the setter for EmbeddedObjects, Collections and/or Entities, when we want to call the getter. This is for initialisation of null-values.
//                //We call the getter before we call the setter and compare if there was a property-change.
                    
//                ClientEntityFactory.GetterItem getterItem =
//                    DictionaryExtension.ValueOrDefault(setterToGetterMethodDict, method.Name);
//                if (getterItem != null)
//                {
//                    HandleSetter(invocation, getterItem);
//                    return;
//                }
//                ClientEntityFactory.GetterItem setterItem =
//                    DictionaryExtension.ValueOrDefault(getterToSetterMethodDict, method.Name);
//                if (setterItem != null)
//                {
//                    HandleGetter(invocation, setterItem);
//                    return;
//                }
//                invocation.Proceed();
//            }
//            finally
//            {
//                alreadyInInterceptorTL.Value = alreadyState;
//            }
//        }

//        public void OnPropertyChanged(String propertyName)
//        {
//            if (PropertyChanged != null)
//            {
//                OnPropertyChanged(new PropertyChangedEventArgs(propertyName));
//            }
//        }

//        public void OnPropertyChanged(PropertyChangedEventArgs args)
//        {
//            if (PropertyChanged != null)
//            {
//                if (cacheModification.Active && proxy is INotifyPropertyChangedSource)
//                {
//                    cacheModification.QueuePropertyChangeEvent((INotifyPropertyChangedSource)proxy, args);
//                    return;
//                }
//                bool alreadyState = alreadyInInterceptorTL.Value;
//                try
//                {
//                    alreadyInInterceptorTL.Value = false;
//                    PropertyChanged.Invoke(proxy, args);
//                }
//                finally
//                {
//                    alreadyInInterceptorTL.Value = alreadyState;
//                }
//            }
//        }

//        public void OnCollectionChanged(Object sender, NotifyCollectionChangedEventArgs e)
//        {
//            if (!cacheModification.Active)
//            {
//                SetToBeUpdated(true);
//            }
//        }
        
//        public void OnChildCollectionChanged(Object sender, NotifyCollectionChangedEventArgs e)
//        {
//            if (e.OldItems != null)
//            {
//                foreach (INotifyPropertyChanged obj in e.OldItems)
//                {
//                    obj.PropertyChanged -= OnChildPropertyChanged;
//                }
//            }
//            //Already happens because "Count" changes
//            //if (e.NewItems != null)
//            //{
//            //    foreach (INotifyPropertyChanged obj in e.NewItems)
//            //    {
//            //        obj.PropertyChanged += OnChildPropertyChanged;
//            //        if (!(proxy is IDataObject) || ((IDataObject)proxy).HasPendingChanges)
//            //        {
//            //            continue;
//            //        }
//            //        if(!CacheModification.IsActive && proxy is IDataObject && obj is IDataObject)
//            //        {
//            //            IDataObject child = (IDataObject)obj;
//            //            bool hasPendingChanges = child.HasPendingChanges;
//            //            if (hasPendingChanges)
//            //            {
//            //                ((IDataObject)proxy).ToBeUpdated = true;
//            //            }
//            //        }
//            //    }
//            //}
//        }
        
//        public void OnChildPropertyChanged(Object sender, PropertyChangedEventArgs e)
//        {
//            if (!"HasPendingChanges".Equals(e.PropertyName))
//            {
//                return;
//            }

//            if (!cacheModification.Active && proxy is IDataObject && sender is IDataObject)
//            {
//                IDataObject child = (IDataObject)sender;
//                bool hasPendingChanges = child.HasPendingChanges;
//                if (hasPendingChanges)
//                {
//                    ((IDataObject)proxy).ToBeUpdated = true;
//                }
//            }
//            //This is the case after a save was successfully commited
//            if (cacheModification.Active && proxy is IDataObject && sender is IDataObject)
//            {
//                IDataObject child = (IDataObject)sender;
//                bool hasPendingChanges = child.HasPendingChanges;
//                if (!hasPendingChanges)
//                {
//                    ((IDataObject)proxy).ToBeUpdated = false;
//                }
//            }
//        }
//    }
//}
