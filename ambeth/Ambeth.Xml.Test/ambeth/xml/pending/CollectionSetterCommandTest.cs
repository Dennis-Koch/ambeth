using System;
using System.Collections;
using System.Collections.Generic;
using System.Linq;
using System.Reflection;
using De.Osthus.Ambeth.Xml.Pending;
using Microsoft.VisualStudio.TestTools.UnitTesting;

namespace De.Osthus.Ambeth.Xml.Test
{
    [TestClass]
    public class CollectionSetterCommandTest
    {
        private CollectionSetterCommand fixture;

        [TestMethod]
        public virtual void testArrayList()
        {
            Object obj = "";
            IList coll = new ArrayList();

            MethodInfo addMethod = coll.GetType().GetMethod("Add", new Type[] { typeof(Object) });

            fixture = new CollectionSetterCommand();
            fixture.ObjectFuture = null;
            fixture.Parent = coll;
            fixture.AddMethod = addMethod;
            fixture.Obj = obj;
            fixture.AfterPropertiesSet();
            fixture.Execute(null);

            Assert.AreEqual(1, coll.Count);
            Assert.AreEqual(obj, coll[0]);
        }

        [TestMethod]
        public virtual void testListOfObject()
        {
            Object obj = "";
            IList<Object> coll = new List<Object>();

            MethodInfo addMethod = coll.GetType().GetMethod("Add", new Type[] { typeof(Object) });

            fixture = new CollectionSetterCommand();
            fixture.ObjectFuture = null;
            fixture.Parent = coll;
            fixture.AddMethod = addMethod;
            fixture.Obj = obj;
            fixture.AfterPropertiesSet();
            fixture.Execute(null);

            Assert.AreEqual(1, coll.Count);
            Assert.AreEqual(obj, coll.ElementAt(0));
        }

        [TestMethod]
        public virtual void TestListOfString()
        {
            Object obj = "";
            IList<String> coll = new List<String>();

            MethodInfo addMethod = coll.GetType().GetMethod("Add", new Type[] { typeof(String) });

            fixture = new CollectionSetterCommand();
            fixture.ObjectFuture = null;
            fixture.Parent = coll;
            fixture.AddMethod = addMethod;
            fixture.Obj = obj;
            fixture.AfterPropertiesSet();
            fixture.Execute(null);

            Assert.AreEqual(1, coll.Count);
            Assert.AreEqual(obj, coll.ElementAt(0));
        }

        [TestMethod]
        public virtual void TestHashSetOfObject()
        {
            Object obj = "";
            ISet<Object> coll = new HashSet<Object>();

            MethodInfo addMethod = coll.GetType().GetMethod("Add", new Type[] { typeof(Object) });

            fixture = new CollectionSetterCommand();
            fixture.ObjectFuture = null;
            fixture.Parent = coll;
            fixture.AddMethod = addMethod;
            fixture.Obj = obj;
            fixture.AfterPropertiesSet();
            fixture.Execute(null);

            Assert.AreEqual(1, coll.Count);
            Assert.AreEqual(obj, coll.ElementAt(0));
        }

        [TestMethod]
        public virtual void TestHashSetOfString()
        {
            Object obj = "";
            ISet<String> coll = new HashSet<String>();

            MethodInfo addMethod = coll.GetType().GetMethod("Add", new Type[] { typeof(String) });

            fixture = new CollectionSetterCommand();
            fixture.ObjectFuture = null;
            fixture.Parent = coll;
            fixture.AddMethod = addMethod;
            fixture.Obj = obj;
            fixture.AfterPropertiesSet();
            fixture.Execute(null);

            Assert.AreEqual(1, coll.Count);
            Assert.AreEqual(obj, coll.ElementAt(0));
        }
    }
}
