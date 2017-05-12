using System;
using System.Collections;
using System.Collections.Generic;
using System.Linq;
using De.Osthus.Ambeth.Collections;
using De.Osthus.Ambeth.Ioc;
using De.Osthus.Ambeth.Ioc.Factory;
using De.Osthus.Ambeth.Merge;
using De.Osthus.Ambeth.Util;
#if SILVERLIGHT
using System.Windows.Browser;
#else
#endif

namespace De.Osthus.Minerva.Core
{
    public class DataConsumerModule : IInitializingModule, IStartingModule, IDisposableBean, ISharedDataHandOn
    {
        //TODO: empty Stack when navigation is finished (e.g. manual uri manipulation)
        public static String SourceUriBeanName = "SourceUri";

        public virtual IRevertChangesHelper RevertChangesHelper { get; set; }

        public virtual ISharedData SharedData { get; set; }

        public virtual ISharedDataHandOnExtendable SharedDataHandOnExtendable { get; set; }

        public virtual String Token { get; set; }

        /// <summary>
        /// This Dictionary defines defaultvalues for beans. <br/>
        /// Values here can have several meanings:
        /// <ul>
        ///  <li>Dictionary null: all params optional</li>
        ///  <li>key null: parameter optional</li>
        ///  <li>value null: parameter mandatory</li>
        ///  <li>value set: param is the default bean</li>
        /// </ul>
        /// </summary>
        public virtual IDictionary<String, String> BeansToConsume { get; set; }

        public virtual void AfterPropertiesSet(IBeanContextFactory beanContextFactory)
        {
            ParamChecker.AssertNotNull(RevertChangesHelper, "RevertChangesHelper");
            ParamChecker.AssertNotNull(SharedData, "SharedData");
            ParamChecker.AssertNotNull(SharedDataHandOnExtendable, "SharedDataHandOnExtendable");

            //TODO: inject Uri as bean
#if SILVERLIGHT
            Uri uri = HtmlPage.Document.DocumentUri;
#else
            Uri uri = null;
            if (uri == null)
            {
                throw new NotSupportedException("This code has to be compatible with .NET first");
            }
#endif

            ISet<String> allBeanNames = new HashSet<String>();
            if (BeansToConsume != null)
            {
                allBeanNames.UnionWith(BeansToConsume.Keys);
            }

            IDictionary<String, IModelContainer> data = null;
            if (Token != null)
            {
                data = SharedData.Read(Token);
            }
            if (data == null)
            {
                // Clear token to suppress handsOn in afterStarted()
                Token = null;
                data = new Dictionary<String, IModelContainer>();
            }
            IModelMultiContainer<Uri> uriList = (IModelMultiContainer<Uri>)DictionaryExtension.ValueOrDefault(data, SourceUriBeanName);
            if (uriList != null)
            {
                //Url-list is avaliable
                uriList.Values.Add(uri);
            }
            allBeanNames.UnionWith(data.Keys);

            if (!allBeanNames.Contains(SourceUriBeanName))
            {
                //Url-list is not avaliable
                beanContextFactory.RegisterBean<ModelMultiContainer<Uri>>(SourceUriBeanName).PropertyValue("Value", uri);
            }

            IdentityHashSet<Object> allProvidedBusinessObjects = new IdentityHashSet<Object>();
            foreach (String nameInOwnContext in allBeanNames)
            {
                //Proecess the input
                IModelContainer dataContainer = DictionaryExtension.ValueOrDefault(data, nameInOwnContext);
                if (dataContainer != null)
                {
                    if (dataContainer is IModelMultiContainer)
                    {
                        IEnumerable businessObjects = ((IModelMultiContainer)dataContainer).ValuesData;
                        if (businessObjects != null)
                        {
                            allProvidedBusinessObjects.AddAll(businessObjects.Cast<object>());
                        }
                    }
                    else if (dataContainer is IModelSingleContainer)
                    {
                        Object businessObject = ((IModelSingleContainer)dataContainer).ValueData;
                        if (businessObject != null)
                        {
                            allProvidedBusinessObjects.Add(businessObject);
                        }
                    }
                    //By copying only the data, listeners are unregistered
                    //beanContextFactory.registerBean(name, dataContainer.GetType()).propertyValue("Data", dataContainer.Data);
                    beanContextFactory.RegisterExternalBean(nameInOwnContext, dataContainer);
                    continue;
                }
                if (!BeansToConsume.ContainsKey(nameInOwnContext))
                {
                    continue;
                }
                //Process default-beans
                String aliasToDefaultBean = BeansToConsume[nameInOwnContext];
                if (aliasToDefaultBean == null)
                {
                    //Mandatory parameter was not present in data
                    throw new Exception("The new Screen has not all mandatory information: \"" + nameInOwnContext + "\" is missing.");
                }
                if (!nameInOwnContext.Equals(aliasToDefaultBean))
                {
                    beanContextFactory.RegisterAlias(nameInOwnContext, aliasToDefaultBean);
                }
            }
            if (allProvidedBusinessObjects.Count > 0)
            {
                IRevertChangesSavepoint savepoint = RevertChangesHelper.CreateSavepoint(allProvidedBusinessObjects);
                beanContextFactory.RegisterExternalBean(savepoint).Autowireable<IRevertChangesSavepoint>();
            }
        }

        public void AfterStarted(IServiceContext serviceContext)
        {
            if (Token != null)
            {
                SharedDataHandOnExtendable.RegisterSharedDataHandOn(this, Token);
            }
        }

        public void Destroy()
        {
            if (Token != null)
            {
                SharedDataHandOnExtendable.UnregisterSharedDataHandOn(this, Token);
            }
        }
    }
}
