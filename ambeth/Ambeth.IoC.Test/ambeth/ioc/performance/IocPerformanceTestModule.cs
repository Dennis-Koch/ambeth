using De.Osthus.Ambeth.Config;
using De.Osthus.Ambeth.Ioc.Factory;
using De.Osthus.Ambeth.Log;

namespace De.Osthus.Ambeth.Ioc.Performance
{
    public class IocPerformanceTestModule : IInitializingModule
    {
        [LogInstance]
        public ILogger Log { private get; set; }

        [Property(IocPerformanceTest.count_prop)]
        public int Count { protected get; set; }

        public void AfterPropertiesSet(IBeanContextFactory beanContextFactory)
        {
            for (int a = Count; a-- > 0; )
            {
                beanContextFactory.RegisterBean("name" + a, typeof(TestBean)).PropertyValue("Value", "value");
            }
        }
    }
}