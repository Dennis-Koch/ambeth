//using System.ServiceModel;
//using System.Threading;
//using De.Osthus.Ambeth.Ioc;
//using De.Osthus.Minerva.Core;
//using System;
//using De.Osthus.Ambeth.Log;

//namespace De.Osthus.Minerva.Util
//{
//    public class WorkerUtil : IWorkerUtil, IInitializingBean
//    {
//        [LogInstance]
//		public ILogger Log { private get; set; }

//        public virtual SynchronizationContext SyncContext { get; set; }

//        public virtual void AfterPropertiesSet()
//        {
//            // Intended blank
//        }

//        public void Fork<T>(GenericViewModel<T> model, WorkerDelegate<T> workerRunnable)
//        {
//            ThreadPool.QueueUserWorkItem(delegate(Object state)
//            {
//                try
//                {
//                    workerRunnable.Invoke(model);
//                }
//                catch (Exception e)
//                {
//                    Log.Error(e);
//                    if (SyncContext != null)
//                    {
//                        SyncContext.Post(delegate(Object innerState)
//                        {
//                            throw new Exception("Background exception occured", e);
//                        }, null);
//                    }
//                }
//            });
//        }
//    }
//}
