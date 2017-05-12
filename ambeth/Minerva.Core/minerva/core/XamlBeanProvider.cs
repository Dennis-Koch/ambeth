using System;
using System.Collections.Generic;
using System.ComponentModel;
using System.Dynamic;
using System.Runtime.CompilerServices;
using System.Text;
using System.Windows;
using De.Osthus.Ambeth.Ioc;
using System.Windows.Markup;
using De.Osthus.Ambeth.Log;
using System.Collections.ObjectModel;
using De.Osthus.Ambeth.Util;
using System.Diagnostics;

namespace De.Osthus.Minerva.Core
{
    public interface IBeanRegistration
    {
        String Type { get; set; }
    }

    public class AnonymousBean : IBeanRegistration
    {
        public virtual String Type { get; set; }
    }

    public class NamedBean : IBeanRegistration
    {
        public virtual String Type { get; set; }
    }

    public class Module : IBeanRegistration
    {
        public virtual String Type { get; set; }
    }

    /// <summary>
    /// ObservableCollection is used to handle Content in Xaml. It's Index-Operator is hidden by this class.
    /// The only restriction is: Do not use Beans in Constructor of your Controls.
    ///  - Sometimes this is a problem: e.g. StyleSelector is actively used in constructor. If StyleSelector is registered as a bean, early usage of it will fail.
    /// </summary>
    public class XamlBeanProvider : ObservableCollection<IBeanRegistration>, INotifyPropertyChanged, IInitializingBean, ISelfRegisteringControlBean, IStartingBean
    {
        [LogInstance]
		public ILogger Log { private get; set; }

        /// <summary>
        /// Adds support for most bindings directly
        /// </summary>
        public static IServiceContext CurrentBeanContext { get; set; }

        public virtual IServiceContext BeanContext { get; set; }

        protected IList<LazyBeanBinding> bindingsToGo = new List<LazyBeanBinding>();

        public virtual bool IsLazyBindingAllowed { get; set; }

        public new event PropertyChangedEventHandler PropertyChanged;

        public void OnPropertyChanged(String propertyName)
        {
            if (PropertyChanged != null)
            {
                PropertyChanged(this, new PropertyChangedEventArgs(propertyName));
            }
        }

        public XamlBeanProvider()
        {
            IsLazyBindingAllowed = true;
        }

        public Object CreateEvaluateLaterBinding(String xamlPath)
        {
            LazyBeanBinding lazyBinding = new LazyBeanBinding(xamlPath);
            bindingsToGo.Add(lazyBinding);
            dynamic placeholder = lazyBinding;
            Debug.WriteLine("Lazy Binding for: " + xamlPath);
            return placeholder;
        }
        /// <summary>
        /// do not get properties inside index.braces []. (wrong: [foo.bar] correct: [foo].bar)
        /// This is not the getter of the baseclass "ObservableCollection".
        /// </summary>
        /// <param name="xamlBeanName"></param>
        /// <returns></returns>
        [IndexerName("Item")]
        public virtual Object this[String xamlBeanName]
        {
            get
            {
                if (CurrentBeanContext != null)
                {
                    Object ret = CurrentBeanContext.GetService(xamlBeanName, false);
                    if (ret != null)
                    {
                        return ret;
                    }
                }
                if (BeanContext != null && BeanContext.IsRunning)
                {
                    if (!IsLazyBindingAllowed)
                    {
                        Debug.WriteLine("Forced Binding: " + xamlBeanName);
                    }
                    return BeanContext.GetService(xamlBeanName);
                }
                if (BeanContext != null && BeanContext.IsDisposed)
                {
                    Debug.WriteLine("Binding after Dispose of BeanContext: " + xamlBeanName);
                    return null;
                }
                if (!IsLazyBindingAllowed)
                {
                    throw new Exception("Lazy Binding is not allowed.");
                }
                return CreateEvaluateLaterBinding(xamlBeanName);
            }
            set
            {
                throw new Exception("It is not allowed to set a value which contains only a beanName without a property: '" + xamlBeanName + "'");
            }
        }

        public void DoLazyBinding()
        {
            IList<LazyBeanBinding> errors = new List<LazyBeanBinding>();
            foreach (LazyBeanBinding binding in bindingsToGo)
            {
                OnPropertyChanged("Item[" + binding.Name + "]");
            }
            if (errors.Count > 0)
            {
                StringBuilder sb = new StringBuilder();

                foreach (LazyBeanBinding err in errors)
                {
                    sb.Append(err.Name);
                    sb.Append(", ");
                }
                sb.Remove(sb.Length - 3, 2);
                throw new Exception("There were illegal bindings: " + sb.ToString());
            }
        }

        public void AfterPropertiesSet()
        {
            // intended blank
        }

        public void AfterStarted()
        {
            IsLazyBindingAllowed = false;
            DoLazyBinding();
        }

        public void RegisterSelf(Ambeth.Ioc.Config.IBeanConfiguration beanConfiguration, IServiceContext beanContext, Ambeth.Ioc.Factory.IBeanContextFactory beanContextFactory)
        {
            foreach (IBeanRegistration br in base.Items)
            {
                try
                {
                    beanContextFactory.RegisterBean(AssemblyHelper.GetTypeFromAssemblies(br.Type));
                }
                catch (Exception e)
                {
                    Log.Error("Type not found: \"" + br.Type + "\"", e);
                }
            }
        }
    }
}
