using De.Osthus.Ambeth.Helloworld.Transfer;
using De.Osthus.Ambeth.Ioc.Annotation;
using De.Osthus.Ambeth.Log;
using De.Osthus.Ambeth.Merge;
using De.Osthus.Ambeth.Merge.Model;
using De.Osthus.Ambeth.Service;
using De.Osthus.Ambeth.Test;
using De.Osthus.Ambeth.Testutil;
using De.Osthus.Ambeth.Threading;
using De.Osthus.Ambeth.Walker;
using Microsoft.VisualStudio.TestTools.UnitTesting;
using System;
using System.ComponentModel;

namespace De.Osthus.Ambeth.Cache.DataChange
{
    [TestRebuildContext]
    [TestClass]
    public class DataChangeTest : AbstractHelloWorldTest
    {
        [LogInstance]
        public new ILogger Log { private get; set; }

        [Autowired]
        public ICacheWalker CacheWalker { protected get; set; }

        [Autowired]
        public IMergeService MergeService { protected get; set; }

        [TestMethod]
        public void Test_DataChange()
        {
            TestEntity newTestEntity = EntityFactory.CreateEntity<TestEntity>();
            CountDownLatch latch = new CountDownLatch(1);
            MergeProcess.Process(newTestEntity, null, null, delegate(bool success)
            {
                latch.CountDown();
            });
            latch.Await();
            WaitForUI();

            newTestEntity.Relation = EntityFactory.CreateEntity<TestEntity2>();

            ICacheWalkerResult cwResult = CacheWalker.WalkEntities(newTestEntity);
            Log.Info(cwResult.ToString());

            ICUDResult mergeResult = MergeController.MergeDeep(newTestEntity, new MergeHandle());

            latch = new CountDownLatch(1);
            RevertChangesHelper.RevertChanges(newTestEntity, delegate(bool success)
            {
                latch.CountDown();
            });
            latch.Await();
            WaitForUI();

            ((IWritableCache)Cache).Put(newTestEntity);
            latch = new CountDownLatch(1);

            ((INotifyPropertyChanged)newTestEntity).PropertyChanged += delegate(Object sender, PropertyChangedEventArgs evnt)
            {
                latch.CountDown();
            };

            MergeService.Merge(mergeResult, null);
            latch.Await();
            WaitForUI();

            Assert.AssertNotNull(newTestEntity.Relation);
        }
    }
}