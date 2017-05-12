using System;
using System.Collections.Generic;
using System.ComponentModel;
using System.IO;
using System.IO.IsolatedStorage;
using System.Reflection;
using System.Text;
using System.Threading;
using System.Windows;
using System.Windows.Input;
using System.Windows.Resources;
using De.Osthus.Ambeth.Cache.Config;
using De.Osthus.Ambeth.Config;
using De.Osthus.Ambeth.Event.Config;
using De.Osthus.Ambeth.Filter.Model;
using De.Osthus.Ambeth.Helloworld.Config;
using De.Osthus.Ambeth.Helloworld.Ioc;
using De.Osthus.Ambeth.Helloworld.Service;
using De.Osthus.Ambeth.Ioc;
using De.Osthus.Ambeth.Ioc.Factory;
using De.Osthus.Ambeth.Log;
using De.Osthus.Ambeth.Merge.Config;
using De.Osthus.Ambeth.Proxy;
using De.Osthus.Ambeth.Security.Config;
using De.Osthus.Ambeth.Transfer;
using De.Osthus.Ambeth.Util;
using De.Osthus.Minerva.Core.Config;
using De.Osthus.Minerva.Ioc;
using De.Osthus.Ambeth.Threading;
using De.Osthus.Ambeth.Merge;
using De.Osthus.Ambeth.CompositeId;
using De.Osthus.Ambeth.Ioc.Annotation;
using De.Osthus.Ambeth.Bytecode;
using De.Osthus.Ambeth.Helloworld.Transfer;
using De.Osthus.Ambeth.Debug;
using De.Osthus.Ambeth.Merge.Model;
using De.Osthus.Ambeth.Cache.Model;
using De.Osthus.Ambeth.Cache;

namespace De.Osthus.Minerva.Client
{
    public partial class App : Application
    {
        protected ILogger Log;

        protected IServiceContext BeanContext;

        public App()
        {
            this.Startup += this.Application_Startup;
            this.Exit += this.Application_Exit;
            this.UnhandledException += this.Application_UnhandledException;

            InitializeComponent();
        }

        private void Application_Startup(object sender, StartupEventArgs e)
        {
            String configFolder = "config";
            String fileName = "Minerva.properties";
            String filePath = configFolder + "\\" + fileName;
            IsolatedStorageFile storage = IsolatedStorageFile.GetUserStoreForSite();

            Stream primaryPropertyStream = null;
            try
            {
                storage.CreateDirectory("config");
                primaryPropertyStream = new IsolatedStorageFileStream(filePath, FileMode.OpenOrCreate, FileAccess.ReadWrite, FileShare.ReadWrite, storage);
            }
            catch (IsolatedStorageException)
            {                
                // Intended blank
            }
            catch (FileNotFoundException)
            {
                // Intended blank
            }

            String urlString = "/Minerva.Client;component/" + filePath;
            StreamResourceInfo streamResourceInfo = Application.GetResourceStream(new Uri(urlString, UriKind.Relative));

            if (streamResourceInfo == null)
            {
#if DEVELOP
                urlString = "/Minerva.Client;component/" + configFolder + "/Osthus.properties";
#else
                urlString = "/Minerva.Client;component/" + configFolder + "/Osthus_Production.properties";
#endif
                streamResourceInfo = Application.GetResourceStream(new Uri(urlString, UriKind.Relative));
            }

            Properties properties = Properties.Application;

            if (streamResourceInfo != null)
            {
                properties.Load(streamResourceInfo.Stream);
            }

            String servicePort = DictionaryExtension.ValueOrDefault(e.InitParams, "serviceport");
            String serviceHost = DictionaryExtension.ValueOrDefault(e.InitParams, "servicehost");

            DictionaryExtension.Loop(e.InitParams, delegate(String key, String value)
            {
                properties.Set(key, value);
            });

            if (servicePort != null)
            {
                properties[ServiceConfigurationConstants.ServiceHostPort] = servicePort;
            }
            if (serviceHost != null)
            {
                properties[ServiceConfigurationConstants.ServiceHostName] = serviceHost;
            }

            properties.Load(primaryPropertyStream);

            Properties.System[ServiceWCFConfigurationConstants.TransferObjectsScope] = ".+";
            Properties.System[EventConfigurationConstants.PollingActive] = "true";
            Properties.System[ServiceConfigurationConstants.NetworkClientMode] = "true";
            Properties.System[ServiceConfigurationConstants.GenericTransferMapping] = "false";
            Properties.System[ServiceConfigurationConstants.IndependentMetaData] = "false";

            properties[ServiceConfigurationConstants.ServiceBaseUrl] = "${" + ServiceConfigurationConstants.ServiceProtocol + "}://"
                + "${" + ServiceConfigurationConstants.ServiceHostName + "}" + ":"
                + "${" + ServiceConfigurationConstants.ServiceHostPort + "}"
                + "${" + ServiceConfigurationConstants.ServicePrefix + "}";

            properties[ServiceConfigurationConstants.ServiceProtocol] = "http";
            properties[ServiceConfigurationConstants.ServiceHostName] = "localhost.";
            properties[ServiceConfigurationConstants.ServiceHostPort] = "9080";
            properties[ServiceConfigurationConstants.ServicePrefix] = "/helloworld";

            LoggerFactory.LoggerType = typeof(De.Osthus.Ambeth.Log.ClientLogger);

            Log = LoggerFactory.GetLogger(typeof(App), properties);

            if (Log.InfoEnabled)
            {
                ISet<String> allPropertiesSet = properties.CollectAllPropertyKeys();

                List<String> allPropertiesList = new List<String>(allPropertiesSet);

                allPropertiesList.Sort();

                Log.Info("Property environment:");
                foreach (String property in allPropertiesList)
                {
                    Log.Info("Property " + property + "=" + properties[property]);
                }
            }

            Type type = storage.GetType();
            PropertyInfo propertyInfo = null;
            while (type != null)
            {
                propertyInfo = type.GetProperty("RootDirectory", System.Reflection.BindingFlags.NonPublic | System.Reflection.BindingFlags.Public);
                if (propertyInfo != null)
                {
                    break;
                }
                type = type.BaseType;
            }
            AssemblyHelper.RegisterAssemblyFromType(typeof(BytecodeModule));
            AssemblyHelper.RegisterAssemblyFromType(typeof(CacheBootstrapModule));
            AssemblyHelper.RegisterAssemblyFromType(typeof(CacheBytecodeModule));
            AssemblyHelper.RegisterAssemblyFromType(typeof(CacheDataChangeBootstrapModule));
            AssemblyHelper.RegisterAssemblyFromType(typeof(DataChangeBootstrapModule));
            AssemblyHelper.RegisterAssemblyFromType(typeof(EventBootstrapModule));
            AssemblyHelper.RegisterAssemblyFromType(typeof(IocBootstrapModule));
            AssemblyHelper.RegisterAssemblyFromType(typeof(MergeBootstrapModule));
            AssemblyHelper.RegisterAssemblyFromType(typeof(CompositeIdModule));
            AssemblyHelper.RegisterAssemblyFromType(typeof(PrivilegeBootstrapModule));
            AssemblyHelper.RegisterAssemblyFromType(typeof(SecurityBootstrapModule));
            AssemblyHelper.RegisterAssemblyFromType(typeof(ServiceBootstrapModule));
            AssemblyHelper.RegisterAssemblyFromType(typeof(MethodDescription));

            AssemblyHelper.RegisterAssemblyFromType(typeof(MinervaCoreBootstrapModule));
            AssemblyHelper.RegisterAssemblyFromType(typeof(RESTBootstrapModule));
            AssemblyHelper.RegisterAssemblyFromType(typeof(XmlBootstrapModule));
            AssemblyHelper.RegisterAssemblyFromType(typeof(FilterDescriptor));

            AssemblyHelper.RegisterAssemblyFromType(typeof(HelloWorldModule));
            AssemblyHelper.InitAssemblies("ambeth\\..+", "minerva\\..+");

            properties[XmlConfigurationConstants.PackageScanPatterns] = @"De\.Osthus(?:\.Ambeth|\.Minerva)(?:\.[^\.]+)*(?:\.Transfer|\.Model|\.Service)\..+";
            properties[CacheConfigurationConstants.FirstLevelCacheType] = "SINGLETON";
            properties[CacheConfigurationConstants.OverwriteToManyRelationsInChildCache] = "false";
            properties[CacheConfigurationConstants.UpdateChildCache] = "true";
            properties[MinervaCoreConfigurationConstants.EntityProxyActive] = "true";
            properties[ServiceConfigurationConstants.TypeInfoProviderType] = typeof(MergeTypeInfoProvider).FullName;

            // Set this to false, to test with mocks (offline)
            bool Online = true;

            properties[CacheConfigurationConstants.CacheServiceBeanActive] = Online.ToString();
            properties[EventConfigurationConstants.EventServiceBeanActive] = Online.ToString();
            properties[MergeConfigurationConstants.MergeServiceBeanActive] = Online.ToString();
            properties[SecurityConfigurationConstants.SecurityServiceBeanActive] = Online.ToString();
            properties[HelloWorldConfigurationConstants.HelloWorldServiceBeanActive] = Online.ToString();
            properties[MergeConfigurationConstants.MergeServiceMockType] = typeof(HelloWorldMergeMock).FullName;
//already default:            properties[RESTConfigurationConstants.HttpAcceptEncodingZipped] = "true";
//already default:            properties[RESTConfigurationConstants.HttpContentEncodingZipped] = "true";
//already default:            properties[RESTConfigurationConstants.HttpUseClient] = "true";

            IServiceContext bootstrapContext = BeanContextFactory.CreateBootstrap(properties);

            try
            {
                // Create child context and override root context
                BeanContext = bootstrapContext.CreateService(delegate(IBeanContextFactory bcf)
                {
                    bcf.RegisterAnonymousBean<MainPageModule>();
                    bcf.RegisterAnonymousBean(typeof(HelloWorldModule));
                    AssemblyHelper.HandleTypesFromCurrentDomainWithAnnotation<FrameworkModuleAttribute>(delegate(Type bootstrapModuleType)
                    {
                        //if (!typeof(IInitializingBootstrapMockModule).IsAssignableFrom(bootstrapModuleType))
                        {
                            if (Log.InfoEnabled)
                            {
                                Log.Info("Autoresolving bootstrap module: '" + bootstrapModuleType.FullName + "'");
                            }
                            bcf.RegisterAnonymousBean(bootstrapModuleType);
                        }
                    });
                    bcf.RegisterExternalBean("app", this);
                }, typeof(RESTBootstrapModule));
                FlattenHierarchyProxy.Context = BeanContext;

                BeanContext.GetService<IThreadPool>().Queue(delegate()
                {
                    double result = BeanContext.GetService<IHelloWorldService>().DoFunnyThings(5, "hallo");
                    double result2 = BeanContext.GetService<IHelloWorldService>().DoFunnyThings(6, "hallo");
                    if (Math.Abs(result - result2) != 1)
                    {
                        throw new Exception("Process execution failed with unexpected result value: " + result + "/" + result2);
                    }
                    Log.Info("" + result);
                    //Type enhancedType = BeanContext.GetService<IBytecodeEnhancer>().GetEnhancedType(typeof(TestEntity), EntityEnhancementHint.HOOK);
                    //TestEntity instance = (TestEntity)Activator.CreateInstance(enhancedType);
                    //instance.Id = 1;
                    //IEntityMetaData metaData = BeanContext.GetService<IEntityMetaDataProvider>().GetMetaData(enhancedType);
                    //IObjRelation result3 = ((IValueHolderContainer)instance).GetSelf("Relation");
                    //Object targetCache = ((IValueHolderContainer)instance).TargetCache;
                    //((IValueHolderContainer)instance).TargetCache = new ChildCache();
                    //Console.WriteLine("TestT");
                });
                SynchronizationContext syncContext = BeanContext.GetService<SynchronizationContext>();

                syncContext.Post((object state) =>
                {
                    RootVisual = BeanContext.GetService<UIElement>("mainPage");
                }, null);

            }
            catch (Exception ex)
            {
                if (Log.ErrorEnabled)
                {
                    Log.Error(ex);
                }
                throw;
            }
        }

        private void Application_Exit(object sender, EventArgs e)
        {
            if (BeanContext != null)
            {
                BeanContext.GetRoot().Dispose();
                BeanContext = null;
            }
        }

        private void Application_UnhandledException(object sender, ApplicationUnhandledExceptionEventArgs e)
        {
            // If the app is running outside of the debugger then report the exception using
            // the browser's exception mechanism. On IE this will display it a yellow alert 
            // icon in the status bar and Firefox will display a script error.
            //if (!System.Diagnostics.Debugger.IsAttached)
            Log.Error(e.ExceptionObject);
            {
                // NOTE: This will allow the application to continue running after an exception has been thrown
                // but not handled. 
                // For production applications this error handling should be replaced with something that will 
                // report the error to the website and stop the application.
                e.Handled = true;
                Deployment.Current.Dispatcher.BeginInvoke(delegate { ReportErrorToDOM(e); });
            }
        }

        private void ReportErrorToDOM(ApplicationUnhandledExceptionEventArgs e)
        {
            try
            {
                string errorMsg = ExtractFullStackTrace(e.ExceptionObject);
                errorMsg = errorMsg.Replace('"', '\'').Replace("\r\n", @"\n");

                System.Windows.Browser.HtmlPage.Window.Eval("throw new Error(\"Unhandled Error in Silverlight Application " + errorMsg + "\");");
            }
            catch (Exception)
            {
            }
        }

        protected String ExtractFullStackTrace(Exception e)
        {
            StringBuilder sb = new StringBuilder();
            sb.Append(e.StackTrace);
            Exception innerException = e.InnerException;
            while (innerException != null)
            {
                sb.Append(Environment.NewLine);
                sb.Append(Environment.NewLine);
                sb.Append(innerException.Message);
                sb.Append(Environment.NewLine);
                sb.Append(innerException.StackTrace);
                innerException = innerException.InnerException;
            }
            return sb.ToString();
        }
    }
}
