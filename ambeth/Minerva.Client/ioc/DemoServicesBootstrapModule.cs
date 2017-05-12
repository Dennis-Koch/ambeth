using System;
using System.Collections.Generic;
using De.Osthus.Ambeth.Event;
using De.Osthus.Ambeth.Service;
using De.Osthus.Ambeth.Config;
using De.Osthus.Ambeth.Event.Config;
using System.Reflection;
using De.Osthus.Ambeth.Log;
using De.Osthus.Ambeth.Service.Config;
using De.Osthus.Ambeth.Ioc.Factory;
using De.Osthus.Ambeth.Proxy;
using System.Threading;
using De.Osthus.Ambeth.Model;
using De.Osthus.Minerva.Bind;
using De.Osthus.Minerva.Model;
using De.Osthus.Minerva.Client;
using De.Osthus.Ambeth.Ioc;
using De.Osthus.Minerva.View;
using De.Osthus.Minerva.Core;
using De.Osthus.Minerva.Service;
using De.Osthus.Ambeth.Cache.Config;
using De.Osthus.Ambeth.Merge.Config;
using Ambeth.Test.ServiceReference1;
using System.ServiceModel.Channels;
using System.ServiceModel;
using De.Osthus.Ambeth.Connection;
using De.Osthus.Ambeth.Test;
using De.Osthus.Ambeth.Service.Interceptor;

namespace De.Osthus.Minerva.Ioc
{
    public class DemoServicesBootstrapModule : IInitializingBootstrapModule, IStartingModule
    {
        [LogInstance]
		public ILogger Log { private get; set; }
                
        public void AfterPropertiesSet(IBeanContextFactory beanContextFactory)
        {
            //Binding binding = WCFClientTargetProvider<Object>.CreateDefaultBinding();
            //EndpointAddress address = new EndpointAddress("http://172.16.1.32:19001/WS_jAmbeth-WS_Test-webapp/GoodBusinessServicePortTypeImplService/GoodBusinessService");
            //GoodBusinessServicePortTypeClient wcfHandle = new GoodBusinessServicePortTypeClient(binding, address);

            //beanContextFactory.registerExternalBean("testService", wcfHandle).autowireable<GoodBusinessServicePortType>();

            //wcfHandle.retriveAllGoodsCompleted += new EventHandler<retriveAllGoodsCompletedEventArgs>(test);  
            //wcfHandle.retriveAllGoodsAsync();

            beanContextFactory.registerBean("dummyBean", typeof(DummyBean));

//            if (IsNetworkClientMode && IsCacheServiceBeanActive)
            {
                beanContextFactory.registerBean<ClientServiceBean>("goodBusinessService")
                    .propertyValue("Interface", typeof(GoodBusinessServicePortTypeSync))
                    .propertyValue("RemoteInterface", typeof(GoodBusinessServicePortType))
                    .autowireable<GoodBusinessServicePortTypeSync>();
            }
        }

        public void AfterStarted(IServiceContext serviceContext)
        {
            //GoodBusinessServicePortType stub = serviceContext.GetService<GoodBusinessServicePortType>();

            //stub.BeginretriveAllGoods(new retriveAllGoodsRequest(), delegate(IAsyncResult ar)
            //{
            //    retriveAllGoodsResponse response1 = stub.EndretriveAllGoods(ar);
            //    Console.WriteLine(response1.@return);
            //}, null);

//            IProxyFactory proxyFactory = serviceContext.GetService<IProxyFactory>();

//            SyncCallInterceptor synchronizedInterceptor = serviceContext.RegisterAnonymousBean<SyncCallInterceptor>().propertyValue("AsyncService", stub).propertyValue("AsyncServiceInterface", typeof(GoodBusinessServicePortType)).finish();

//            GoodBusinessServicePortTypeSync syncStub = (GoodBusinessServicePortTypeSync)proxyFactory.CreateProxy(typeof(GoodBusinessServicePortTypeSync), synchronizedInterceptor);

            ThreadPool.QueueUserWorkItem(delegate(Object state)
            {
                

                serviceContext.GetService<DummyBean>("dummyBean").hallo();
            }, null);
        }

        public void test(Object obj, retriveAllGoodsCompletedEventArgs args)
        {
            Console.WriteLine(args.Result);
        }
    }
}
