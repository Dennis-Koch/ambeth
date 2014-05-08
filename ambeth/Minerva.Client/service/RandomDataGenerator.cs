using De.Osthus.Ambeth.Ioc;
using De.Osthus.Ambeth.Log;
using De.Osthus.Ambeth.Helloworld.Service;
using De.Osthus.Ambeth.Util;
using De.Osthus.Ambeth.Helloworld.Transfer;
using System.Collections.Generic;
using System.Threading;
using System;
using De.Osthus.Ambeth.Merge.Model;
using De.Osthus.Ambeth.Cache.Transfer;
using De.Osthus.Ambeth.Merge.Transfer;
using De.Osthus.Ambeth.Datachange.Transfer;
using De.Osthus.Ambeth.Datachange.Model;
using De.Osthus.Ambeth.Cache;
using De.Osthus.Ambeth.Merge;

namespace De.Osthus.Ambeth.Helloworld
{
    public class RandomDataGenerator : IInitializingBean, IStartingBean
    {
        [LogInstance]
		public ILogger Log { private get; set; }

        public enum ChangeOperation
        {
            NOTHING, INSERT, UPDATE, DELETE
        }

        public virtual IHelloWorldService HelloWorldService { get; set; }

        public virtual ICacheContext CacheContext { get; set; }

        public virtual ICacheProvider CacheProvider { get; set; }

        public virtual IRevertChangesHelper RevertChangesHelper { get; set; }

        protected List<IObjRef> entity2Refs = new List<IObjRef>();

        protected List<IObjRef> entityRefs = new List<IObjRef>();

        protected int valueIncrement;

        protected int valueIncrement2;

        protected int testEntityIdSeq = 10000, testEntity2IdSeq = 10000;

        protected Random random = new Random();

        public virtual void AfterPropertiesSet()
        {
            ParamChecker.AssertNotNull(CacheContext, "CacheContext");
            ParamChecker.AssertNotNull(CacheProvider, "CacheProvider");
            ParamChecker.AssertNotNull(HelloWorldService, "HelloWorldService");
            ParamChecker.AssertNotNull(RevertChangesHelper, "RevertChangesHelper");
        }

        public virtual void AfterStarted()
        {
            ThreadPool.QueueUserWorkItem(delegate(Object obj)
            {
                try
                {
                    while (true)
                    {
                        execute();
                    }
                }
                catch (Exception e)
                {
                    Log.Error(e);
                }
            }, null);
        }

        public virtual void execute()
        {
            long start = Environment.TickCount;

            // Do something for nearly 60 seconds (job gets invoked every 60 seconds
            while (Environment.TickCount - start < 58000)
            {
                IList<TestEntity> allTestEntities = null;
                IList<TestEntity2> allTest2Entities = null;

                ICache newCache = CacheProvider.GetCurrentCache();

                CacheContext.ExecuteWithCache<Object>(newCache, delegate()
                {
                    allTestEntities = HelloWorldService.GetAllTestEntities();
                    allTest2Entities = HelloWorldService.GetAllTest2Entities();
                    return null;
                });

                if (allTestEntities.Count > 0)
                {
                    IRevertChangesSavepoint savepoint = RevertChangesHelper.CreateSavepoint(allTestEntities);
                    allTestEntities[0].MyString = "Hallo";
                    allTestEntities[0].Version = -67;
                    allTestEntities[0].Relation = null;

                    savepoint.RevertChanges();
                }


                // Evaluate random type of change ( INSERT / UPDATE / DELETE / NOTHING)
                double randomChange = random.NextDouble();

                bool entity2Change = random.NextDouble() > 0.66;

                // Evaluate entity to change by its index in the result list of existing entities (necessary for UPDATE /
                // DELETE)
                int changeIndex = (int)(random.NextDouble() * allTestEntities.Count);

                // Evaluate entity2 to select its index in the result list of existing entities (necessary for INSERT of
                // entity1)
                int selectEntity2Index = (int)(random.NextDouble() * allTest2Entities.Count);

                // Evaluate new value to change on chosen entity (necessary for INSERT / UPDATE)
                int randomNewValue = (int)(random.NextDouble() * Int32.MaxValue / 2);

                // Map from randomChange to the enum-based operation to execute
                ChangeOperation changeOperation;
                if (randomChange < 0.10) // 10% probability for INSERTs
                {
                    changeOperation = ChangeOperation.INSERT;
                }
                else if (randomChange < 0.20) // 10% probability for DELETEs
                {
                    changeOperation = ChangeOperation.DELETE;
                }
                else if (randomChange < 0.40) // 20% probability for doing NOTHING
                {
                    changeOperation = ChangeOperation.NOTHING;
                }
                else
                // 60% probablity for doing an ordinary UPDATE on an entity
                {
                    changeOperation = ChangeOperation.UPDATE;
                }
                if (entity2Change && allTestEntities.Count > 0)
                {
                    // If there are less than 10 entities, force to insertion of one
                    if (allTest2Entities.Count < 10)// || ChangeOperation.INSERT.Equals(changeOperation))
                    {
                        TestEntity2 newEntity = new TestEntity2();
                        newEntity.MyValue2 = randomNewValue;
                        HelloWorldService.SaveTestEntities2(newEntity);
                        allTest2Entities.Add(newEntity);
                    }
                    //// If there are more than 20 entities, force to deletion of one
                    else if (allTestEntities.Count > 20 || ChangeOperation.DELETE.Equals(changeOperation))
                    {
                        TestEntity2 deleteEntity = allTest2Entities[selectEntity2Index];
                        allTest2Entities.RemoveAt(selectEntity2Index);
                        HelloWorldService.DeleteTestEntities2(deleteEntity);
                    }
                    else if (ChangeOperation.UPDATE.Equals(changeOperation))
                    {
                        TestEntity2 updateEntity = allTest2Entities[selectEntity2Index];
                        updateEntity.MyValue2 = randomNewValue;
                        HelloWorldService.SaveTestEntities2(updateEntity);
                    }
                    else
                    {
                        TestEntity2 noOpEntity = allTest2Entities[selectEntity2Index];
                        // Change nothing, but try to save entity (results in NO-OP)
                        HelloWorldService.SaveTestEntities2(noOpEntity);
                    }
                }
                else
                {
                    // If there are less than 10 entities, force to insertion of one
                    if (allTestEntities.Count < 10)// || ChangeOperation.INSERT.Equals(changeOperation))
                    {
                        TestEntity newEntity = new TestEntity();
                        newEntity.MyValue = randomNewValue;
                        HelloWorldService.SaveTestEntities(newEntity);
                        allTestEntities.Add(newEntity);
                    }
                    // If there are more than 20 entities, force to deletion of one
                    else if (allTestEntities.Count > 20 || ChangeOperation.DELETE.Equals(changeOperation))
                    {
                        TestEntity deleteEntity = allTestEntities[changeIndex];
                        allTestEntities.RemoveAt(changeIndex);
                        HelloWorldService.DeleteTestEntities(deleteEntity);
                    }
                    else if (ChangeOperation.UPDATE.Equals(changeOperation))
                    {
                        TestEntity updateEntity = allTestEntities[changeIndex];
                        TestEntity2 testEntity2 = allTest2Entities.Count > 0 ? allTest2Entities[selectEntity2Index] : null;
                        updateEntity.MyValue = randomNewValue;
                        updateEntity.Relation = testEntity2;
                        updateEntity.EmbeddedObject.Name = "Name_" + randomNewValue;
                        updateEntity.EmbeddedObject.Value = randomNewValue;
                        HelloWorldService.SaveTestEntities(updateEntity);
                    }
                    else
                    {
                        TestEntity noOpEntity = allTestEntities[changeIndex];
                        // Change nothing, but try to save entity (results in NO-OP)
                        HelloWorldService.SaveTestEntities(noOpEntity);
                    }
                }
                Thread.Sleep(500);
            }
        }
    }
}