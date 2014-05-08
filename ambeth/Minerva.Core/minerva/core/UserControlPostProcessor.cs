using System;
using System.Windows.Controls;
using De.Osthus.Ambeth.Ioc;
using De.Osthus.Ambeth.Ioc.Factory;
using De.Osthus.Ambeth.Util;
using System.Windows;
using System.Collections.Generic;
using De.Osthus.Ambeth.Collections;
using System.Reflection;
using De.Osthus.Minerva.Core;
using System.Collections;
using System.Linq;
using System.Text;
using De.Osthus.Ambeth.Ioc.Config;
#if SILVERLIGHT
using Telerik.Windows.Controls;
#else
#endif

namespace De.Osthus.Minerva.Bind
{
    public class UserControlPostProcessor : IBeanPostProcessor, IInitializingBean
    {
        protected WeakDictionary<IBeanContextFactory, ISet<Object>> factoryToAlreadyHandledNames = new WeakDictionary<IBeanContextFactory, ISet<Object>>();

        public virtual void AfterPropertiesSet()
        {
        }

        public virtual Object PostProcessBean(IBeanContextFactory beanContextFactory, IServiceContext beanContext, IBeanConfiguration beanConfiguration, Type beanType, Object targetBean, ISet<Type> requestedTypes)
        {
            if (!typeof(FrameworkElement).IsAssignableFrom(beanType))
            {
                // Handle only FrameworkElements
                return targetBean;
            }
            if (beanType.IsAssignableFrom(typeof(UserControl)))
            {
                // Ignore all instances which are base types of UserControl
                return targetBean;
            }

            ISet<Object> alreadyHandledBeans = factoryToAlreadyHandledNames[beanContextFactory];
            if (alreadyHandledBeans == null)
            {
                alreadyHandledBeans = new IdentityHashSet<Object>();
                factoryToAlreadyHandledNames[beanContextFactory] = alreadyHandledBeans;
            }
            if (alreadyHandledBeans.Contains(targetBean))
            {
                //Do not yet add the Bean to the list.
                return targetBean;
            }

            FrameworkElement frameworkElement = (FrameworkElement)targetBean;
            MethodInfo initializeComponentMethod = beanType.GetMethod("InitializeComponent");
            if (initializeComponentMethod != null)
            {
                IServiceContext oldCurrentBeanContext = XamlBeanProvider.CurrentBeanContext;
                try
                {
                    XamlBeanProvider.CurrentBeanContext = beanContext;
                    initializeComponentMethod.Invoke(targetBean, null);
                }
                catch (Exception e)
                {
                    throw new Exception("InitializeComponent of \"" + frameworkElement.Name + "\" (" + beanType.FullName + ") failed.", e);
                }
                finally
                {
                    XamlBeanProvider.CurrentBeanContext = oldCurrentBeanContext;
                }
            }

            if (!typeof(UIElement).IsAssignableFrom(beanType))
            {
                return targetBean;
            }

            ISet<Object> unnamedBeans = new IdentityHashSet<Object>();
            IDictionary<String, Object> namedBeans = new Dictionary<String, Object>();
            CollectChildBeans((UIElement)targetBean, unnamedBeans, namedBeans, alreadyHandledBeans);

            foreach (Object unnamedBean in unnamedBeans)
            {
                IBeanConfiguration nestedBeanConfiguration = beanContextFactory.RegisterWithLifecycle(unnamedBean);
                if (unnamedBean is ISelfRegisteringControlBean)
                {
                    ((ISelfRegisteringControlBean)unnamedBean).RegisterSelf(nestedBeanConfiguration, beanContext, beanContextFactory);
                }
            }
            foreach (KeyValuePair<String, Object> namedBean in namedBeans)
            {
                Object currentNamedBean = namedBean.Value;
                IBeanConfiguration nestedBeanConfiguration = beanContextFactory.RegisterWithLifecycle(namedBean.Key, currentNamedBean);
                if (currentNamedBean is ISelfRegisteringControlBean)
                {
                    ((ISelfRegisteringControlBean)currentNamedBean).RegisterSelf(nestedBeanConfiguration, beanContext, beanContextFactory);
                }
            }
            if (targetBean is ISelfRegisteringControlBean)
            {
                ((ISelfRegisteringControlBean)targetBean).RegisterSelf(beanConfiguration, beanContext, beanContextFactory);
            }
            return targetBean;
        }

        protected void CollectChildBeans(UIElement parent, ISet<Object> foundUnnamedBeans, IDictionary<String, Object> foundNamedBeans, ISet<Object> alreadyHandledBeans)
        {
            CollectChildBeans(parent, foundUnnamedBeans, foundNamedBeans, alreadyHandledBeans, "");
        }

        protected void CollectChildBeans(Object current, ISet<Object> foundUnnamedBeans, IDictionary<String, Object> foundNamedBeans, ISet<Object> alreadyHandledBeans, String namePrefix)
        {
            if (current == null || !alreadyHandledBeans.Add(current))
            {
                return;
            }

            //TODO: Check for "IInitializingBean" here, if to many Elements are registered (profiling).
            // This would lead to an API/Interface Change.
            if (namePrefix == null)
            {
                foundUnnamedBeans.Add(current);
            }
            else if (namePrefix != "") //do nothing for the root element, it was registered before PostProcessor was invoked
            {
                foundNamedBeans.Add(namePrefix, current);
                namePrefix += "/";
            }

            Type currentType = current.GetType();

            if (!(current is FrameworkElement))
            {
                //We can not retrieve children from the current object
                return;
            }
            FrameworkElement fElement = (FrameworkElement)current;
            IDictionaryEnumerator enumerator = fElement.Resources.GetEnumerator();
            while (enumerator.MoveNext())
            {
                DictionaryEntry entry = (DictionaryEntry)enumerator.Current;
                Object key = entry.Key;
                String newName = null;
                if (key is String)
                {
                    newName = namePrefix + (String)key;
                }
                CollectChildBeans(entry.Value, foundUnnamedBeans, foundNamedBeans, alreadyHandledBeans, newName);
            }

            //Find all fields and try to get their names
            IList<FieldInfo> fields = GetBeanFieldInfosFromType(currentType);
            foreach (FieldInfo fieldInfo in fields)
            {
                Object foundElement = fElement.FindName(fieldInfo.Name);
                String childName;
                if (namePrefix == null)
                {
                    childName = null;
                }
                else
                {
                    childName = namePrefix + fieldInfo.Name;
                }
                CollectChildBeans(foundElement, foundUnnamedBeans, foundNamedBeans, alreadyHandledBeans, childName);
            }
#if SILVERLIGHT
            CollectChildBeans(RadContextMenu.GetContextMenu(fElement), foundUnnamedBeans, foundNamedBeans, alreadyHandledBeans, null); ;
#endif

            if (fElement is System.Windows.Controls.ItemsControl)
            {
                System.Windows.Controls.ItemsControl itemsControl = (System.Windows.Controls.ItemsControl)fElement;
                foreach (Object element in itemsControl.Items)
                {
                    CollectChildBeans(element, foundUnnamedBeans, foundNamedBeans, alreadyHandledBeans, null);
                }
            }

            //Handle all Panel Children. We have no chance to find a name here. If a Child has a name, it was already handled in the code above
            if (fElement is Panel)
            {
                Panel parentPanel = (Panel)fElement;
                foreach (UIElement element in parentPanel.Children)
                {
                    CollectChildBeans(element, foundUnnamedBeans, foundNamedBeans, alreadyHandledBeans, null);
                }
            }
        }

        protected IList<FieldInfo> GetBeanFieldInfosFromType(Type type)
        {
            IList<FieldInfo> finalfields = new List<FieldInfo>();
            FieldInfo[] fields = type.GetFields(BindingFlags.NonPublic | BindingFlags.Instance);

            Type subClassSystemType = type;
            while (subClassSystemType != null
                && !subClassSystemType.BaseType.Namespace.StartsWith("System.Windows"))
            {
                subClassSystemType = subClassSystemType.BaseType;
            }

            for (int a = fields.Length; a-- > 0; )
            {
                FieldInfo field = fields[a];

                String fieldName = field.Name;
                if (fieldName.Contains("<"))
                {
                    // Autogenerated compiler fields of properties have such characters
                    continue;
                }
                if (subClassSystemType != null && !subClassSystemType.IsAssignableFrom(field.DeclaringType))
                {
                    // Ignore all fields which are declared at UserControl and its parent types
                    continue;
                }
                if ("_contentLoaded".Equals(fieldName))
                {
                    // Ignore the one and only field which falls through the first two checks
                    continue;
                }
                if (field.FieldType.IsValueType)
                {
                    // Ignore ValueTypes
                    continue;
                }
                finalfields.Add(field);
            }
            return finalfields;
        }
    }
}

