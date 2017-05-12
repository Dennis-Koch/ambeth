using De.Osthus.Ambeth.Cache.Config;
using De.Osthus.Ambeth.Config;
using De.Osthus.Ambeth.Helloworld.Config;
using De.Osthus.Ambeth.Helloworld.Service;
using De.Osthus.Ambeth.Helloworld.Transfer;
using De.Osthus.Ambeth.Ioc;
using De.Osthus.Ambeth.Ioc.Factory;
using De.Osthus.Ambeth.Log;
using De.Osthus.Ambeth.Proxy;
using De.Osthus.Ambeth.Util;
using De.Osthus.Minerva.Core;
using De.Osthus.Ambeth.Typeinfo;

namespace De.Osthus.Ambeth.Helloworld.Ioc
{
    public class HelloWorldModule : IInitializingModule
    {
        [LogInstance]
		public ILogger Log { private get; set; }

        [Property(HelloWorldConfigurationConstants.HelloWorldServiceBeanActive, DefaultValue = "true")]
        public bool IsHelloWorldBeanActive { get; set; }

        [Property(ServiceConfigurationConstants.NetworkClientMode, DefaultValue = "false")]
        public bool IsNetworkClientMode { get; set; }

        public IProxyFactory ProxyFactory { get; set; }

        public ITypeInfoProvider TypeInfoProvider { get; set; }

        public void AfterPropertiesSet(IBeanContextFactory beanContextFactory)
        {
            ParamChecker.AssertNotNull(ProxyFactory, "ProxyFactory");
            // One of maybe hundred models of any type
            beanContextFactory.RegisterBean<GenericViewModel<TestEntity>>("model_TestEntity_all");

            beanContextFactory.RegisterBean<ModelMultiContainer<TestEntity>>("selected_TestEntity_all");

            beanContextFactory.RegisterBean<AllTestEntitiesRefresher>("refresher_TestEntity_all");

            ViewModelDataChangeController<TestEntity> vmdccTestEntity = (ViewModelDataChangeController<TestEntity>)
                beanContextFactory.RegisterBean<ViewModelDataChangeController<TestEntity>>("controller_TestEntity_all")
                .PropertyRefs("refresher_TestEntity_all", "model_TestEntity_all").GetInstance();

            vmdccTestEntity.AddRelationPath("Relation");

            beanContextFactory.RegisterBean<GenericViewModel<TestEntity2>>("model_TestEntity2_all");

            beanContextFactory.RegisterBean<AllTest2EntitiesRefresher>("refresher_TestEntity2_all");

            beanContextFactory.RegisterBean<ViewModelDataChangeController<TestEntity2>>("controller_TestEntity2_all")
                .PropertyRefs("refresher_TestEntity2_all", "model_TestEntity2_all").GetInstance();
            
            IHelloWorldService service = ProxyFactory.CreateProxy<IHelloWorldService>();
            beanContextFactory.RegisterExternalBean("client.helloWorldService", service).Autowireable<IHelloWorldService>();

            if (!IsNetworkClientMode || !IsHelloWorldBeanActive)
            {
                beanContextFactory.RegisterBean<HelloWorldServiceMock>("helloWorldServiceMock");

                beanContextFactory.RegisterBean<RandomDataGenerator>("randomDataGenerator").PropertyRefs(CacheNamedBeans.CacheProviderPrototype);
            }

            // Data output
            // beanContextFactory.registerBean<DataProvider>("dataOutput_model_TestEntity_all").propertyRef("Data", "model_TestEntity_all");

            // Data input
            // beanContextFactory.registerBean<DataConsumer>("model_TestEntity_all_2").propertyValue("Token", "myTokenInSharedData");
        }
    }
}
