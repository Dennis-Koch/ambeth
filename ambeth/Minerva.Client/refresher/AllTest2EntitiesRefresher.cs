using De.Osthus.Ambeth.Helloworld.Service;
using De.Osthus.Ambeth.Helloworld.Transfer;
using De.Osthus.Ambeth.Util;
using De.Osthus.Minerva.Core;
using System.Collections.Generic;

namespace De.Osthus.Ambeth.Helloworld
{
    public class AllTest2EntitiesRefresher : AbstractRefresher<TestEntity2>
    {
        public IHelloWorldService TestEntityService { get; set; }

        public override void AfterPropertiesSet()
        {
            base.AfterPropertiesSet();

            ParamChecker.AssertNotNull(TestEntityService, "TestEntityService");
        }

        public override IList<TestEntity2> Populate(params object[] contextInformation)
        {
            return TestEntityService.GetAllTest2Entities();
        }
    }
}
