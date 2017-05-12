using De.Osthus.Ambeth.Annotation;
using De.Osthus.Ambeth.Filter.Model;
using De.Osthus.Ambeth.Helloworld.Transfer;
using De.Osthus.Ambeth.Proxy;
using System;
using System.Collections.Generic;

namespace De.Osthus.Ambeth.Helloworld.Service
{
    [ServiceClient(Name = "HelloWorldService")]
    [XmlType]
    public interface IHelloWorldService
    {
        [Find]
        IPagingResponse FindTestEntities(IFilterDescriptor filterDescriptor, ISortDescriptor[] sortDescriptors, IPagingRequest pagingRequest);

        [Find]
        IList<TestEntity> GetAllTestEntities();

        [Find]
        IList<TestEntity2> GetAllTest2Entities();

        double DoFunnyThings(int value, String text);

        [Merge]
        void SaveTestEntities(params TestEntity[] testEntities);

        [Merge]
        void SaveTestEntities(IEnumerable<TestEntity> testEntities);

        [Remove]
        void DeleteTestEntities(params TestEntity[] testEntities);

        [Remove]
        void DeleteTestEntities(IEnumerable<TestEntity> testEntities);

        [Merge]
        void SaveTestEntities2(params TestEntity2[] testEntities);

        [Merge]
        void SaveTestEntities2(IEnumerable<TestEntity2> testEntities);

        [Remove]
        void DeleteTestEntities2(params TestEntity2[] testEntities);

        [Remove]
        void DeleteTestEntities2(IEnumerable<TestEntity2> testEntities);
    }
}
