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
using System.Reflection;
using De.Osthus.Minerva.Extendable;
using De.Osthus.Ambeth.Ioc;
using System.Collections.Generic;
using De.Osthus.Ambeth.Util;

namespace De.Osthus.Minerva.Core
{
    public class DataProvider : IInitializingBean, IDataProvider, IStartingBean, IDisposableBean, ISharedDataHandOn
    {
        public virtual ISharedData SharedData { get; set; }

        public ISharedDataHandOnExtendable SharedDataHandOnExtendable { get; set; }

        public virtual IDictionary<String, String> Data { get; set; }

        public virtual String Token { get; protected set; }

        public virtual IServiceContext BeanContext { get; set; }

        public virtual void AfterPropertiesSet()
        {
            ParamChecker.AssertNotNull(SharedData, "SharedData");
            ParamChecker.AssertNotNull(SharedDataHandOnExtendable, "SharedDataHandOnExtendable");
            //ParamChecker.AssertNotNull(Data, "Data");
            if (Data == null)
            {
                Token = null;
                return;
            }
            IDictionary<String, IModelContainer> output = new Dictionary<String, IModelContainer>();

            foreach (String name in Data.Keys)
            {
                String beanName = DictionaryExtension.ValueOrDefault<String, String>(Data, name);
                if (beanName == null)
                {
                    //Output with value == null should be published with the same name as in the previous context
                    beanName = name;
                }
                output[name] = BeanContext.GetService<IModelContainer>(beanName);
            }
            output[DataConsumerModule.SourceUriBeanName] = BeanContext.GetService<IModelContainer>(DataConsumerModule.SourceUriBeanName);
            Token = SharedData.Put(output);
            //TODO: How to get the source URI saved for GoBack-Action?
        }

        public void AfterStarted()
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
