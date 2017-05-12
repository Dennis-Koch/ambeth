//using System;
//using System.Collections.Generic;
//using System.Threading;
//using De.Osthus.Ambeth.Ioc;
//using De.Osthus.Ambeth.Ioc.Factory;
//using De.Osthus.Ambeth.Ioc.Hierarchy;
//using Microsoft.VisualStudio.TestTools.UnitTesting;
//using De.Osthus.Ambeth.Util;
//using De.Osthus.Ambeth.Log;
//using De.Osthus.Ambeth.Ioc.Extendable;
//using De.Osthus.Ambeth.Ioc.Exceptions;
//using System.Collections.ObjectModel;
//using De.Osthus.Ambeth.Threading;

//namespace De.Osthus.Ambeth.Test.ambeth.ioc
//{
//[TestClass]
//public class ClassExtendableContainerTest
//{
//    [LogInstance]
//    public ILogger Log { private get; set; }

//    protected ClassExtendableContainer<Object> fixture;

//    [TestInitialize]
//    public void setUp()
//    {
//        fixture = new ClassExtendableContainer<Object>("object", "type", false);
//    }

//     [TestCleanup]
//    public void tearDown()
//    {
//        fixture = null;
//    }


//    [TestMethod]
//    [ExpectedException(typeof(ArgumentException))]
//    public void extensionNull()
//    {
//        fixture.Register(null, typeof(List<Object>));
//    }

//    [TestMethod]
//    [ExpectedException(typeof(ArgumentException))]
//    public void typeNull()
//    {
//        fixture.Register(new Object(), null);
//    }

//    [TestMethod]
//    public void registerSimple()
//    {
//        Object obj1 = new Object();
//        fixture.Register(obj1, typeof(List<Object>));
//        Assert.AreSame(obj1, fixture.GetExtension(typeof(List<Object>)), "Registration failed");
//        Assert.AreEqual(1, fixture.GetExtensions().Count, "Registration failed");
//        Assert.AreSame(obj1, fixture.GetExtensions()[typeof(List<Object>)], "Registration failed");
//    }

//[TestMethod]
//    [ExpectedException(typeof(ExtendableException))]
//    public void duplicateStrong()
//    {
//        fixture.Register(new Object(), typeof(List<Object>));
//        fixture.Register(new Object(), typeof(List<Object>));
//    }

//    [TestMethod]
//    public void strongAndWeakSimple()
//    {
//        Object obj1 = new Object();
//        fixture.Register(obj1, typeof(ICollection<Object>));

//        Assert.AreSame(obj1, fixture.GetExtension(typeof(List<Object>)), "Strong registration failed");
//        //Assert.AreSame(obj1, fixture.GetExtension(typeof(List<Object>)), "Weak registration to parent class failed");
//        Assert.AreSame(obj1, fixture.GetExtension(typeof(IList<Object>)), "Weak registration to interface failed");
//        Assert.AreSame(obj1, fixture.GetExtension(typeof(IEnumerable<Object>)), "Weak registration to parent interface failed");
//    }

//    [TestMethod]
//    public void strongAndWeak()
//    {
//        Object obj1 = new Object();
//        Object obj2 = new Object();
//        fixture.Register(obj1, typeof(List<Object>));
//        fixture.Register(obj2, typeof(List<Int32>));

//        Assert.AreSame(obj1, fixture.GetExtension(typeof(List<Object>)), "Registration failed somehow");
//        Assert.AreSame(obj2, fixture.GetExtension(typeof(List<Int32>)), "Registration failed somehow");
//        Assert.AreSame(obj2, fixture.GetExtension(typeof(IList<Int32>)), "Weak registration failed");
//    }

//    /**
//     * Checks that unregistering an extension from a specific type does interfere neither ith other specific types of same extension nor with other extensions
//     */
//    [TestMethod]
//    public void strongIsolated()
//    {
//        fixture = new ClassExtendableContainer<Object>("object", "type", true);

//        Object obj1 = new Object();
//        Object obj2 = new Object();
//        fixture.Register(obj1, typeof(List<Object>));
//        fixture.Register(obj1, typeof(IList<Object>));
//        fixture.Register(obj1, typeof(ICollection<Object>));
//        fixture.Register(obj2, typeof(LinkedList<Object>));
//        fixture.Register(obj2, typeof(ICollection<Object>));

//        Assert.AreSame(obj2, fixture.GetExtension(typeof(LinkedList<Object>)));
//        Assert.AreSame(obj1, fixture.GetExtension(typeof(List<Object>)));
//        Assert.AreSame(obj1, fixture.GetExtension(typeof(IList<Object>)));
//        Assert.AreSame(obj1, fixture.GetExtension(typeof(ICollection<Object>)));

//        fixture.Unregister(obj1, typeof(IList<Object>));

//        Assert.AreSame(obj1, fixture.GetExtension(typeof(List<Object>)));
//        Assert.AreSame(obj1, fixture.GetExtension(typeof(IList<Object>)));
//        Assert.AreSame(obj1, fixture.GetExtension(typeof(ICollection<Object>)));

//        fixture.Unregister(obj1, typeof(List<Object>));

//        Assert.AreSame(obj1, fixture.GetExtension(typeof(List<Object>)));
//        Assert.AreSame(obj1, fixture.GetExtension(typeof(IList<Object>)));
//        Assert.AreSame(obj1, fixture.GetExtension(typeof(ICollection<Object>)));

//        fixture.Unregister(obj1, typeof(ICollection<Object>));

//        Assert.AreSame(obj2, fixture.GetExtension(typeof(List<Object>)));
//        Assert.AreSame(obj2, fixture.GetExtension(typeof(IList<Object>)));
//        Assert.AreSame(obj2, fixture.GetExtension(typeof(ICollection<Object>)));
//    }

//    [TestMethod]
//    public void multiThreaded()
//    {
//        Object obj1 = new Object();
//        Object obj2 = new Object();

//        CyclicBarrier barrier1 = new CyclicBarrier(2);
//        CyclicBarrier barrier2 = new CyclicBarrier(2);
//        CyclicBarrier barrier3 = new CyclicBarrier(2);
//        CyclicBarrier barrier4 = new CyclicBarrier(2);
//        CyclicBarrier barrier5 = new CyclicBarrier(2);

//        CountDownLatch finishLatch = new CountDownLatch(2);

//        ParamHolder<Throwable> ex = new ParamHolder<Throwable>();

//        Runnable run1 = new Runnable()
//        {
//            @Override
//            public void run()
//            {
//                try
//                {
//                    barrier1.await();

//                    fixture.register(obj1, typeof(List<Object>));
//                    fixture.register(obj2, typeof(IList<Object>));
//                    fixture.register(obj1, typeof(ICollection<Object>));

//                    barrier2.await();

//                    barrier3.await();

//                    fixture.unregister(obj2, typeof(IList<Object>));
//                    barrier4.await();
//                    barrier5.await();
//                }
//                catch (Throwable e)
//                {
//                    ex.setValue(e);
//                    while (finishLatch.getCount() > 0)
//                    {
//                        finishLatch.countDown();
//                    }
//                }
//                finally
//                {
//                    finishLatch.countDown();
//                }
//            }
//        };

//        Runnable run2 = new Runnable()
//        {
//            @Override
//            public void run()
//            {
//                try
//                {
//                    barrier1.await();
//                    barrier2.await();

//                    Assert.assertSame(obj2, fixture.GetExtension(typeof(LinkedList<Object>)));
//                    Assert.assertSame(obj1, fixture.GetExtension(typeof(List<Object>)));
//                    Assert.assertSame(obj2, fixture.GetExtension(typeof(IList<Object>)));
//                    Assert.assertSame(obj1, fixture.GetExtension(typeof(ICollection<Object>)));

//                    barrier3.await();
//                    barrier4.await();
//                    Assert.assertSame(obj1, fixture.GetExtension(typeof(LinkedList<Object>)));
//                    Assert.assertSame(obj1, fixture.GetExtension(typeof(List<Object>)));
//                    Assert.assertSame(obj1, fixture.GetExtension(typeof(IList<Object>)));
//                    Assert.assertSame(obj1, fixture.GetExtension(typeof(ICollection<Object>)));
//                    barrier5.await();
//                }
//                catch (Throwable e)
//                {
//                    ex.setValue(e);
//                    while (finishLatch.getCount() > 0)
//                    {
//                        finishLatch.countDown();
//                    }
//                }
//                finally
//                {
//                    finishLatch.countDown();
//                }
//            }
//        };

//        Thread thread1 = new Thread(run1);
//        thread1.setDaemon(true);
//        thread1.start();

//        Thread thread2 = new Thread(run2);
//        thread2.setDaemon(true);
//        thread2.start();

//        finishLatch.await();

//        if (ex.getValue() != null)
//        {
//            throw ex.getValue();
//        }
//    }
//}
//}