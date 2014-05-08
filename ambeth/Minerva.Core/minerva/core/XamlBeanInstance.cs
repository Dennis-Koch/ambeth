using System;
using System.Net;
using System.Windows;
using System.Windows.Controls;
using System.Windows.Documents;
using System.Windows.Ink;
using System.Windows.Input;
using System.Windows.Media;
using System.Windows.Media.Animation;
using System.Windows.Shapes;
using System.ComponentModel;
using De.Osthus.Ambeth.Ioc;
using De.Osthus.Ambeth.Util;
using System.Dynamic;
using System.Collections.Generic;
using System.Reflection;
using De.Osthus.Ambeth.Log;
using System.Collections.Specialized;
using De.Osthus.Ambeth.Proxy;
using System.Collections.ObjectModel;
using System.Threading;

namespace De.Osthus.Minerva.Core
{
    public class XamlBeanInstance : DynamicObject, INotifyPropertyChanged, INotifyCollectionChanged, IInitializingBean, IStartingBean
    {
        [LogInstance]
		public ILogger Log { private get; set; }

        protected static readonly ThreadLocal<IServiceContext> currentBeanContextTL = new ThreadLocal<IServiceContext>();

        public static IServiceContext CurrentBeanContext
        {
            get
            {
                return currentBeanContextTL.Value;
            }
            set
            {
                currentBeanContextTL.Value = value;
            }
        }

        public event PropertyChangedEventHandler PropertyChanged;
        //{
        //    add
        //    {
        //        Log.Info("P ADD " + value.Target);               
        //    }
        //    remove
        //    {
        //        Log.Info("P REM " + value.Target);             
        //    }
        //}

        public event NotifyCollectionChangedEventHandler CollectionChanged;
        //{
        //    add
        //    {
        //        Log.Info("C ADD " + value.Target);             
        //    }
        //    remove
        //    {
        //        Log.Info("C REM " + value.Target);             
        //    }
        //}

        public String BeanAndPropertyPath { get; set; }

        public String BeanName { get; set; }

        public IServiceContext BeanContext { get; set; }

        protected bool started = false;

        protected IDictionary<String, Object> pendingPropertiesDict;

        public XamlBeanInstance()
        {
            BeanContext = CurrentBeanContext;
        }

        public virtual void AfterPropertiesSet()
        {
            if (BeanContext == null)
            {
                BeanContext = CurrentBeanContext;
            }
            ParamChecker.AssertNotNull(BeanAndPropertyPath, "BeanAndPropertyPath");
            ParamChecker.AssertNotNull(BeanContext, "BeanContext");
            ParamChecker.AssertNotNull(BeanName, "BeanName");            
        }

        public virtual void AfterStarted()
        {
            // Ensure bean of given name exists in context. An exception will occur on any integrity error
            Object beanInstance = BeanContext.GetService(BeanName);

            started = true;

            if (beanInstance is INotifyPropertyChanged)
            {
                BeanContext.Link(new PropertyChangedEventHandler(this.HandlePropertyChangedOfBean)).To(BeanName, INotifyPropertyChangedEvents.PropertyChanged);
            }
            if (beanInstance is INotifyCollectionChanged)
            {
                BeanContext.Link(new NotifyCollectionChangedEventHandler(this.HandleCollectionChangedOfBean)).To(BeanName, INotifyCollectionChangedEvents.CollectionChanged);
            }

            if (pendingPropertiesDict != null)
            {
                DictionaryExtension.Loop(pendingPropertiesDict, delegate(String propertyName, Object value)
                {
                    ((IList<Object>)pendingPropertiesDict[propertyName]).Add(new Object());
                    RaisePropertyChanged(propertyName);
                });
            }
        }

        protected void HandlePropertyChangedOfBean(Object sender, PropertyChangedEventArgs e)
        {
            RaisePropertyChanged(e.PropertyName);
        }

        protected void HandleCollectionChangedOfBean(Object sender, NotifyCollectionChangedEventArgs e)
        {
            RaisePropertyChanged("Objects");
            RaiseCollectionChanged(e);
        }

        protected Object GetParentObject(String xamlBeanName, out String propertyName)
        {
            // This simple "cache" makes sense in order to fix a known bug in Silverlight 4 where a dynamic property resolving will be called exactly 6 times
            // where 1 time would obviously be enough. This simple cache will leverage a potential performance issue while seeking for the bean in the IOC hierarchy
            // Maybe SL 5 will fix necessity...
            String[] tokens = xamlBeanName.Split('.');
            String rootBeanName = tokens[0];

            Object bean = BeanContext.GetService(rootBeanName);

            for (int a = 1, size = tokens.Length - 1; a < size; a++)
            {
                bean = GetPropertyOfObject(bean, tokens[a]);
                if (bean == null)
                {
                    break;
                }
            }
            propertyName = (tokens.Length > 1 ? tokens[tokens.Length - 1] : null);
            return bean;
        }

        protected Object GetPropertyOfObject(Object obj, String propertyName)
        {
            PropertyInfo property = obj.GetType().GetProperty(propertyName);
            if (property == null)
            {
                throw new ArgumentException("Property '" + propertyName + "' not found on bean path");
            }
            return property.GetValue(obj, null);
        }

        public Object this[String propertyName]
        {
            get
            {
                if (!((ServiceContext)BeanContext).IsRunning)
                {
                    if (pendingPropertiesDict == null)
                    {
                        pendingPropertiesDict = new Dictionary<String, Object>();
                    }
                    pendingPropertiesDict[propertyName] = new ObservableCollection<Object>();
                    return pendingPropertiesDict[propertyName];
                }
                Object bean = BeanContext.GetService(BeanName);
                if (propertyName.Contains("."))
                {
                    throw new Exception("It is not allowed to specify hierarchic databinding: " + propertyName);
                }
                return GetPropertyOfObject(bean, propertyName);
            }
            set
            {
                if (!((ServiceContext)BeanContext).IsRunning)
                {
                    if (pendingPropertiesDict == null)
                    {
                        pendingPropertiesDict = new Dictionary<String, Object>();
                    }
                    pendingPropertiesDict[propertyName] = value;
                    return;
                }
                Object bean = BeanContext.GetService(BeanName);
                if (propertyName.Contains("."))
                {
                    throw new Exception("It is not allowed to specify hierarchic databinding: " + propertyName);
                }
                bean.GetType().GetProperty(propertyName).SetValue(bean, value, null);
                RaisePropertyChanged(propertyName);
            }
        }

        public override bool TryGetMember(GetMemberBinder binder, out Object result)
        {
            result = this[binder.Name];
            return (result != null); // Valid beans are always non-null
        }

        public override bool TrySetMember(SetMemberBinder binder, Object value)
        {
            String binderName = binder.Name;
            String propertyName;
            Object parentObj = GetParentObject(binderName, out propertyName);
            if (propertyName == null)
            {
                return false;
            }
            parentObj.GetType().GetProperty(propertyName).SetValue(parentObj, value, null);
            RaisePropertyChanged(binderName);
            return true;
        }

        public override bool TryGetIndex(GetIndexBinder binder, object[] indexes, out object result)
        {
            return base.TryGetIndex(binder, indexes, out result);
        }

        public override bool TrySetIndex(System.Dynamic.SetIndexBinder binder, object[] indexes, object value)
        {
            return base.TrySetIndex(binder, indexes, value);
        }

        protected void RaisePropertyChanged(String propertyName)
        {
            if (PropertyChanged != null)
            {
                PropertyChanged(this, new PropertyChangedEventArgs(propertyName));
            }
        }

        protected void RaiseCollectionChanged(NotifyCollectionChangedEventArgs e)
        {
            if (CollectionChanged != null)
            {
                CollectionChanged(this, e);
            }
        }
    }
}
