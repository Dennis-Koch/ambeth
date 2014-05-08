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
using Telerik.Windows.Controls;
using De.Osthus.Ambeth.Ioc;
using De.Osthus.Ambeth.Util;
using De.Osthus.Ambeth.Ioc.Factory;
using De.Osthus.Ambeth.Ioc.Hierarchy;
using De.Osthus.Ambeth.Ioc.Config;
using System.Threading;

namespace De.Osthus.Minerva.Dialogs
{
    public class WindowFactory : IWindowFactory, IInitializingBean
    {
        public virtual IServiceContext BeanContext { get; set; }

        

        public virtual void AfterPropertiesSet()
        {
            ParamChecker.AssertNotNull(BeanContext, "BeanContext");
        }

        public IBeanContextHolder<RadWindow> CreateDefaultWindow()
        {
            return null;
            /*
            return CreateDefaultWindow(null, null);
             * */
        }

        public IBeanContextHolder<RadWindow> CreateDefaultWindow(String header, UserControl content)
        {
            String windowBeanName = "window";
            IBeanContextHolder<RadWindow> radWindow = BeanContext.CreateService<RadWindow>(delegate(IBeanContextFactory childContextFactory)
            {
                childContextFactory.RegisterExternalBean("param.header", header);

                IBeanConfiguration window = childContextFactory.RegisterBean<RadWindow>(windowBeanName)
                    .PropertyRef("Header", "param.header")
                    .PropertyValue("WindowStartupLocation", Telerik.Windows.Controls.WindowStartupLocation.CenterScreen)
                    .PropertyValue("CanClose", "true")
                    .PropertyValue("CanMove", "true")
                    .PropertyValue("ResizeMode", ResizeMode.CanResize)
                    .Autowireable<RadWindow>();

                if (content != null)
                {
                    // Optional content argument
                    childContextFactory.RegisterExternalBean("param.content", content);

                    window.PropertyRef("Content", "param.content")
                        .PropertyValue("Width", content.Width + 20)
                        .PropertyValue("Height", content.Height + 50);
                }
                UserControl parent = Application.Current.RootVisual as UserControl;
                if (parent != null)
                {
                    window.PropertyValue("MaxHeight", parent.ActualHeight)
                        .PropertyValue("MaxWidth", parent.ActualWidth);
                }
                if (parent != null && content != null)
                {
                    window.PropertyValue("MinHeight", Math.Min(content.MinHeight, parent.ActualHeight))
                        .PropertyValue("MinWidth", Math.Min(content.MinWidth, parent.ActualWidth));
                }

            });

            radWindow.LinkExtendable.Link(new RoutedEventHandler(DefaultWindow_Loaded)).To(windowBeanName, RadWindowEvents.Loaded);
            radWindow.LinkExtendable.Link(new SizeChangedEventHandler(DefaultWindow_SizeChanged)).To(windowBeanName, RadWindowEvents.SizeChanged);

            return radWindow;
        }

        public IBeanContextHolder<RadWindow> CreateDefaultWindow(String header, Type contentType, params Type[] additionalModules)
        {
            /*
            String windowBeanName = "window";
            IBeanContextHolder<RadWindow> radWindow = BeanContext.CreateService<RadWindow>(delegate(IBeanContextFactory childContextFactory)
            {
                childContextFactory.registerExternalBean("param.header", header);
                
                IBeanConfiguration window = childContextFactory.registerBean<RadWindow>(windowBeanName)
                    .propertyRef("Header", "param.header")
                    .propertyValue("WindowStartupLocation", Telerik.Windows.Controls.WindowStartupLocation.CenterScreen)
                    .propertyValue("CanClose", "true")
                    .propertyValue("CanMove", "true")
                    .propertyValue("ResizeMode", ResizeMode.CanResize)
                    .autowireable<RadWindow>();

                if (contentType != null)
                {
                    // Optional content argument
                    childContextFactory.registerBean("param.content", contentType);

                    window.propertyRef("Content", "param.content");
                        //.propertyValue("Width", content.Width + 20)
                        //.propertyValue("Height", content.Height + 50);
                }
                
                UserControl parent = Application.Current.RootVisual as UserControl;
                if (parent != null)
                {
                    window.propertyValue("MaxHeight", parent.ActualHeight)
                        .propertyValue("MaxWidth", parent.ActualWidth)
                        .propertyValue("MinHeight", Math.Min(content.MinHeight, parent.ActualHeight))
                        .propertyValue("MinWidth", Math.Min(content.MinWidth, parent.ActualWidth));
                }
            });

            radWindow.LinkExtendable.LinkToEvent(windowBeanName, RadWindowEvents.Loaded, new RoutedEventHandler(DefaultWindow_Loaded));
            radWindow.LinkExtendable.LinkToEvent(windowBeanName, RadWindowEvents.SizeChanged, new SizeChangedEventHandler(DefaultWindow_SizeChanged));

            return radWindow;*/
            return null;
        }

        #region event handler functions
        public void DefaultWindow_Loaded(Object sender, RoutedEventArgs e)
        {
            //((FrameworkElement)sender).SizeChanged += new SizeChangedEventHandler(DefaultWindow_SizeChanged);
        }

        public void DefaultWindow_SizeChanged(object sender, SizeChangedEventArgs e)
        {
            RadWindow window = sender as RadWindow;
            UserControl parent = Application.Current.RootVisual as UserControl;
            if (parent != null)
            {
                if (window.Top + window.ActualHeight > parent.ActualHeight)
                {
                    window.Top = (parent.ActualHeight - window.ActualHeight);
                }
                if (window.Left + window.ActualWidth > parent.ActualWidth)
                {
                    window.Left = (parent.ActualWidth - window.ActualWidth);
                }
            }
        }
        #endregion
    }
}
