using De.Osthus.Ambeth.Cache;
using De.Osthus.Ambeth.Filter.Model;
using De.Osthus.Ambeth.Helloworld.Transfer;
using De.Osthus.Ambeth.Ioc;
using De.Osthus.Ambeth.Proxy;
using De.Osthus.Ambeth.Util;
using De.Osthus.Minerva.Mock;
using System;
using System.Collections.Generic;

namespace De.Osthus.Ambeth.Helloworld.Service
{
    [Service(Name = "HelloWorldService")]
    public class HelloWorldServiceMock : IHelloWorldService, IInitializingBean
    {
        public virtual ICache Cache { get; set; }

        public virtual IPersistenceMock PersistenceMock { get; set; }

        public virtual void AfterPropertiesSet()
        {
            ParamChecker.AssertNotNull(Cache, "Cache");
            ParamChecker.AssertNotNull(PersistenceMock, "PersistenceMock");
        }

        public virtual IPagingResponse FindTestEntities(IFilterDescriptor filterDescriptor, ISortDescriptor[] sortDescriptors, IPagingRequest pagingRequest)
        {
            IList<Object> allIdsOfTypeInCache = PersistenceMock.GetAllIds<TestEntity>();
            IList<Object> result = Cache.GetObjects(typeof(TestEntity), allIdsOfTypeInCache);
            PagingResponse pagingResponse = new PagingResponse();

            if (pagingRequest != null)
            {
                pagingResponse.Number = pagingRequest.Number;
            }
            pagingResponse.Result = result;
            return pagingResponse;
        }

        public virtual IList<TestEntity> GetAllTestEntities()
        {
            IList<Object> allIdsOfTypeInCache = PersistenceMock.GetAllIds<TestEntity>();
            return Cache.GetObjects<TestEntity>(allIdsOfTypeInCache);
        }

        public virtual IList<TestEntity2> GetAllTest2Entities()
        {
            IList<Object> allIdsOfTypeInCache = PersistenceMock.GetAllIds<TestEntity2>();
            return Cache.GetObjects<TestEntity2>(allIdsOfTypeInCache);
        }

        public virtual double DoFunnyThings(int value, String text)
        {
            return 5.777643;
        }

        public virtual void SaveTestEntities(params TestEntity[] testEntities)
        {
            throw new NotSupportedException();
        }

        public virtual void SaveTestEntities(IEnumerable<TestEntity> testEntities)
        {
            throw new NotSupportedException();
        }

        public virtual void DeleteTestEntities(params TestEntity[] testEntities)
        {
            throw new NotSupportedException();
        }

        public virtual void DeleteTestEntities(IEnumerable<TestEntity> testEntities)
        {
            throw new NotSupportedException();
        }

        public virtual void SaveTestEntities2(params TestEntity2[] testEntities)
        {
            throw new NotSupportedException();
        }

        public virtual void SaveTestEntities2(IEnumerable<TestEntity2> testEntities)
        {
            throw new NotSupportedException();
        }

        public virtual void DeleteTestEntities2(params TestEntity2[] testEntities)
        {
            throw new NotSupportedException();
        }

        public virtual void DeleteTestEntities2(IEnumerable<TestEntity2> testEntities)
        {
            throw new NotSupportedException();
        }

    }
}
