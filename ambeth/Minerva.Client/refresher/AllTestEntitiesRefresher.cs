using De.Osthus.Ambeth.Helloworld.Service;
using De.Osthus.Ambeth.Helloworld.Transfer;
using De.Osthus.Ambeth.Ioc;
using De.Osthus.Ambeth.Ioc.Annotation;
using De.Osthus.Ambeth.Util;
using De.Osthus.Minerva.Core;
using System;
using System.Collections.Generic;

namespace De.Osthus.Ambeth.Helloworld
{
    public class AllTestEntitiesRefresher : AbstractRefresher<TestEntity>, IStartingBean
    {
        [Autowired]
        public IPrefetchHelper PrefetchHelper { protected get; set; }

        [Autowired]
        public IHelloWorldService TestEntityService { protected get; set; }

        protected IPrefetchHandle prefetch;

        public virtual void AfterStarted()
        {
            prefetch = PrefetchHelper.CreatePrefetch().Add(typeof(TestEntity), "Relations").Build();
        }

        public override IList<TestEntity> Populate(params object[] contextInformation)
        {
            //FilterDescriptor filter = new FilterDescriptor();
            //SortDescriptor sort = new SortDescriptor();
            //sort.Member = "MyValue";
            //sort.SortDirection = SortDirection.ASCENDING;
            //IPagingResponse pagingResponse = TestEntityService.FindTestEntities(filter, new ISortDescriptor[] {sort}, null);
            //IList<Object> result = pagingResponse.Result;
            //IList<TestEntity> typedResult = new List<TestEntity>(result.Count);
            //for (int a = 0, size = result.Count; a < size; a++)
            //{
            //    typedResult.Add((TestEntity)result[a]);
            //}
            //return typedResult;
            //return TestEntityService.GetAllTestEntities();
            IList<TestEntity> result = TestEntityService.GetAllTestEntities();
            prefetch.Prefetch(result);
            foreach (TestEntity testEntity in result)
            {
                int count = testEntity.Relations.Count;
                Console.WriteLine(count);
            }
            return result;
        }
    }
}
